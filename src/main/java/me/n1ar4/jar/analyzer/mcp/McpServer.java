/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.mcp;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import me.n1ar4.jar.analyzer.mcp.protocol.McpMethods;
import me.n1ar4.jar.analyzer.mcp.transport.SseSession;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 手撸的 MCP HTTP Server
 * 支持 SSE 与 Streamable HTTP 两种 MCP 传输方式
 *
 * <p>路由</p>
 * <ul>
 *  <li>GET  /          - 服务信息</li>
 *  <li>GET  /sse       - SSE 长连接（建立 session 并 push event:endpoint）</li>
 *  <li>POST /message   - SSE 端的请求入口（与 sessionId 配套）</li>
 *  <li>POST /mcp       - Streamable HTTP 入口（支持单 JSON 或 SSE 流响应）</li>
 *  <li>GET  /mcp       - Streamable HTTP 流（保留通道）</li>
 * </ul>
 */
public class McpServer {
    private static final Logger logger = LogManager.getLogger();

    private final McpConfig config;
    private final ExecutorService workers;
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running = false;

    // SSE 会话表
    private final Map<String, SseSession> sseSessions = new ConcurrentHashMap<>();
    // Streamable HTTP 当前活跃流响应数
    private final AtomicInteger streamableActive = new AtomicInteger(0);

    // 事件监听
    private volatile McpEventListener listener;

    public McpServer(McpConfig config) {
        this.config = config;
        this.workers = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "mcp-worker");
            t.setDaemon(true);
            return t;
        });
    }

    public void setListener(McpEventListener listener) {
        this.listener = listener;
    }

    public McpConfig getConfig() {
        return config;
    }

    public synchronized void start() throws IOException {
        if (running) return;
        serverSocket = new ServerSocket();
        // 复用 + 绑定
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(config.getBind(), config.getPort()));
        running = true;

        acceptThread = new Thread(this::acceptLoop, "mcp-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
        log("MCP server listening on " + config.getBind() + ":" + config.getPort());
        log("  SSE        : " + (config.isEnableSse() ? "enabled" : "disabled")
                + " (GET /sse, POST /message)");
        log("  Streamable : " + (config.isEnableStreamable() ? "enabled" : "disabled")
                + " (POST /mcp, GET /mcp)");
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
        // 关闭所有 SSE 会话
        for (SseSession s : sseSessions.values()) {
            s.close();
        }
        sseSessions.clear();
        notifyConnections();
        try {
            workers.shutdownNow();
        } catch (Throwable ignored) {
        }
        log("MCP server stopped");
    }

    public boolean isRunning() {
        return running;
    }

    public int getSseSessionCount() {
        return sseSessions.size();
    }

    public int getStreamableActive() {
        return streamableActive.get();
    }

    // ------------------------------------------------------------
    // ACCEPT
    // ------------------------------------------------------------
    private void acceptLoop() {
        while (running) {
            try {
                final Socket sock = serverSocket.accept();
                workers.submit(() -> handleSocket(sock));
            } catch (IOException e) {
                if (running) {
                    warn("accept error: " + e.getMessage());
                }
                break;
            }
        }
    }

    // ------------------------------------------------------------
    // ONE CONNECTION
    // ------------------------------------------------------------
    private void handleSocket(Socket sock) {
        try {
            // SSE 是长连接，所以这里不能马上 try-with-resources 关闭
            // 只有在解析完路由后，对应分支自己负责关闭
            HttpRequest req = readRequest(sock);
            if (req == null) {
                safeClose(sock);
                return;
            }
            // 鉴权
            if (config.isAuth() && !checkAuth(req)) {
                writeSimple(sock, 401, "Unauthorized",
                        "{\"error\":\"need token\"}", "application/json");
                safeClose(sock);
                return;
            }
            route(sock, req);
        } catch (Throwable ex) {
            warn("conn error: " + ex.getMessage());
            safeClose(sock);
        }
    }

    private boolean checkAuth(HttpRequest req) {
        // 支持 Header: Token 或 Authorization: Bearer xxx
        String t1 = req.headers.get("Token");
        if (t1 == null) t1 = req.headers.get("token");
        if (t1 != null && t1.equals(config.getToken())) return true;
        String authz = req.headers.get("Authorization");
        if (authz == null) authz = req.headers.get("authorization");
        if (authz != null && authz.startsWith("Bearer ")) {
            return authz.substring("Bearer ".length()).equals(config.getToken());
        }
        return false;
    }

    private void route(Socket sock, HttpRequest req) throws IOException {
        String path = req.path;
        String method = req.method.toUpperCase();
        // OPTIONS - CORS 预检
        if ("OPTIONS".equals(method)) {
            handleOptions(sock);
            return;
        }
        if ("/".equals(path) || "/info".equals(path)) {
            handleInfo(sock);
            return;
        }
        if ("/sse".equals(path)) {
            if (!config.isEnableSse()) {
                writeSimple(sock, 404, "Not Found",
                        "SSE transport disabled", "text/plain");
                safeClose(sock);
                return;
            }
            if ("GET".equals(method)) {
                handleSseGet(sock);
                // 不关闭 socket，由 SseSession 负责
                return;
            }
            writeSimple(sock, 405, "Method Not Allowed", "use GET", "text/plain");
            safeClose(sock);
            return;
        }
        if ("/message".equals(path)) {
            if (!config.isEnableSse()) {
                writeSimple(sock, 404, "Not Found",
                        "SSE transport disabled", "text/plain");
                safeClose(sock);
                return;
            }
            handleSseMessage(sock, req);
            return;
        }
        if ("/mcp".equals(path)) {
            if (!config.isEnableStreamable()) {
                writeSimple(sock, 404, "Not Found",
                        "Streamable HTTP disabled", "text/plain");
                safeClose(sock);
                return;
            }
            handleStreamable(sock, req);
            return;
        }
        writeSimple(sock, 404, "Not Found",
                "no route: " + path, "text/plain");
        safeClose(sock);
    }

    // ------------------------------------------------------------
    // INFO
    // ------------------------------------------------------------
    private void handleInfo(Socket sock) throws IOException {
        JSONObject info = new JSONObject();
        info.put("name", McpMethods.SERVER_NAME);
        info.put("version", McpMethods.SERVER_VERSION);
        info.put("protocolVersion", McpMethods.PROTOCOL_VERSION);
        info.put("transports", new String[]{"sse", "streamable-http"});
        info.put("auth", config.isAuth());
        info.put("sseSessions", sseSessions.size());
        info.put("streamableActive", streamableActive.get());
        writeSimple(sock, 200, "OK", info.toJSONString(), "application/json");
        safeClose(sock);
    }

    private void handleOptions(Socket sock) throws IOException {
        OutputStream out = sock.getOutputStream();
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 204 No Content\r\n");
        appendCommonHeaders(sb);
        sb.append("Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n");
        sb.append("Access-Control-Allow-Headers: Content-Type, Authorization, Token, MCP-Session-Id, Last-Event-ID\r\n");
        sb.append("Access-Control-Max-Age: 86400\r\n");
        sb.append("Content-Length: 0\r\n");
        sb.append("Connection: close\r\n\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.flush();
        safeClose(sock);
    }

    // ------------------------------------------------------------
    // SSE TRANSPORT
    // ------------------------------------------------------------
    private void handleSseGet(Socket sock) throws IOException {
        sock.setSoTimeout(0); // 不超时（长连接）
        OutputStream out = sock.getOutputStream();

        // 写 SSE 响应头
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        sb.append("Content-Type: text/event-stream; charset=utf-8\r\n");
        sb.append("Cache-Control: no-cache, no-transform\r\n");
        sb.append("Connection: keep-alive\r\n");
        sb.append("X-Accel-Buffering: no\r\n");
        appendCommonHeaders(sb);
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.flush();

        SseSession session = new SseSession(sock, out);
        sseSessions.put(session.getSessionId(), session);
        notifyConnections();
        log("SSE session opened: " + session.getSessionId() + " from " + session.getRemote());

        try {
            // 推送 endpoint 事件 - 告诉客户端 POST 到何处
            String endpoint = "/message?sessionId=" + session.getSessionId();
            session.sendEvent("endpoint", endpoint);

            // 心跳：每 25 秒一个注释行
            while (running && !session.isClosed()) {
                try {
                    Thread.sleep(25000);
                } catch (InterruptedException ie) {
                    break;
                }
                if (session.isClosed()) break;
                try {
                    session.sendComment("ping");
                } catch (IOException ioe) {
                    // 客户端断开
                    break;
                }
            }
        } finally {
            sseSessions.remove(session.getSessionId());
            session.close();
            notifyConnections();
            log("SSE session closed: " + session.getSessionId());
        }
    }

    private void handleSseMessage(Socket sock, HttpRequest req) throws IOException {
        String sessionId = req.query.get("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            writeSimple(sock, 400, "Bad Request",
                    "{\"error\":\"missing sessionId\"}", "application/json");
            safeClose(sock);
            return;
        }
        SseSession session = sseSessions.get(sessionId);
        if (session == null || session.isClosed()) {
            writeSimple(sock, 404, "Not Found",
                    "{\"error\":\"session not found\"}", "application/json");
            safeClose(sock);
            return;
        }

        // 解析请求体
        JSONObject msg = parseJsonBody(req);
        if (msg == null) {
            writeSimple(sock, 400, "Bad Request",
                    "{\"error\":\"invalid json\"}", "application/json");
            safeClose(sock);
            return;
        }

        // 立即返回 202 Accepted
        writeSimple(sock, 202, "Accepted", "", "text/plain");
        safeClose(sock);

        // 异步处理后通过 SSE 通道推送响应
        workers.submit(() -> {
            String mname = msg.getString("method");
            JSONObject resp = McpMethods.dispatch(msg);
            boolean ok = resp != null && resp.get("error") == null;
            requestLog("sse", mname, ok);
            if (resp != null) {
                try {
                    session.sendEvent("message", resp.toJSONString());
                } catch (IOException e) {
                    warn("sse push failed: " + e.getMessage());
                }
            }
        });
    }

    // ------------------------------------------------------------
    // STREAMABLE HTTP TRANSPORT
    // ------------------------------------------------------------
    private void handleStreamable(Socket sock, HttpRequest req) throws IOException {
        if ("GET".equals(req.method.toUpperCase())) {
            // 客户端打开 SSE 通道用于服务端主动推送
            // jar-analyzer 当前没有主动推送场景，返回 405 也符合规范
            writeSimple(sock, 405, "Method Not Allowed",
                    "no server-initiated stream", "text/plain");
            safeClose(sock);
            return;
        }
        if (!"POST".equals(req.method.toUpperCase())) {
            writeSimple(sock, 405, "Method Not Allowed",
                    "use POST", "text/plain");
            safeClose(sock);
            return;
        }

        JSONObject msg = parseJsonBody(req);
        if (msg == null) {
            writeSimple(sock, 400, "Bad Request",
                    "{\"error\":\"invalid json\"}", "application/json");
            safeClose(sock);
            return;
        }

        // 检查 Accept 头：text/event-stream 才使用 SSE 流式
        String accept = req.headers.get("Accept");
        if (accept == null) accept = req.headers.get("accept");
        boolean wantStream = accept != null && accept.contains("text/event-stream");

        if (wantStream) {
            handleStreamableSseResponse(sock, msg);
        } else {
            handleStreamableJsonResponse(sock, msg);
        }
    }

    private void handleStreamableJsonResponse(Socket sock, JSONObject msg) throws IOException {
        // 通知没有响应
        if (!msg.containsKey("id")) {
            String mname = msg.getString("method");
            requestLog("streamable", mname, true);
            // 处理但不响应内容
            McpMethods.dispatch(msg);
            writeSimple(sock, 202, "Accepted", "", "text/plain");
            safeClose(sock);
            return;
        }

        String mname = msg.getString("method");
        JSONObject resp = McpMethods.dispatch(msg);
        boolean ok = resp != null && resp.get("error") == null;
        requestLog("streamable", mname, ok);
        String body = resp == null ? "{}" : resp.toJSONString();

        OutputStream out = sock.getOutputStream();
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        sb.append("Content-Type: application/json; charset=utf-8\r\n");
        sb.append("Content-Length: ").append(data.length).append("\r\n");
        // 给客户端一个 session 标识（可选）
        sb.append("Mcp-Session-Id: ").append(UUID.randomUUID().toString().replace("-", "")).append("\r\n");
        appendCommonHeaders(sb);
        sb.append("Connection: close\r\n\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.write(data);
        out.flush();
        safeClose(sock);
    }

    private void handleStreamableSseResponse(Socket sock, JSONObject msg) throws IOException {
        sock.setSoTimeout(0);
        OutputStream out = sock.getOutputStream();
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 200 OK\r\n");
        sb.append("Content-Type: text/event-stream; charset=utf-8\r\n");
        sb.append("Cache-Control: no-cache, no-transform\r\n");
        sb.append("Connection: keep-alive\r\n");
        sb.append("X-Accel-Buffering: no\r\n");
        appendCommonHeaders(sb);
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.flush();

        streamableActive.incrementAndGet();
        notifyConnections();
        try {
            // 通知则不响应
            if (!msg.containsKey("id")) {
                String mname = msg.getString("method");
                McpMethods.dispatch(msg);
                requestLog("streamable", mname, true);
                return;
            }
            String mname = msg.getString("method");
            JSONObject resp = McpMethods.dispatch(msg);
            boolean ok = resp != null && resp.get("error") == null;
            requestLog("streamable", mname, ok);
            String body = resp == null ? "{}" : resp.toJSONString();
            // 严格遵循 SSE 多行 data 拆分
            StringBuilder evt = new StringBuilder();
            evt.append("event: message\n");
            for (String line : body.split("\n", -1)) {
                evt.append("data: ").append(line).append("\n");
            }
            evt.append("\n");
            out.write(evt.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        } finally {
            streamableActive.decrementAndGet();
            notifyConnections();
            safeClose(sock);
        }
    }

    // ------------------------------------------------------------
    // HTTP UTILS - 手撸的轻量 HTTP/1.1 解析
    // ------------------------------------------------------------
    private HttpRequest readRequest(Socket sock) throws IOException {
        sock.setSoTimeout(15000);
        InputStream in = sock.getInputStream();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.ISO_8859_1));
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) return null;
        String[] parts = requestLine.split("\\s+");
        if (parts.length < 2) return null;
        HttpRequest req = new HttpRequest();
        req.method = parts[0];
        String fullUri = parts[1];
        int q = fullUri.indexOf('?');
        if (q >= 0) {
            req.path = fullUri.substring(0, q);
            decodeQuery(fullUri.substring(q + 1), req.query);
        } else {
            req.path = fullUri;
        }
        // headers
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) break;
            int idx = line.indexOf(':');
            if (idx > 0) {
                req.headers.put(line.substring(0, idx).trim(),
                        line.substring(idx + 1).trim());
            }
        }
        // body
        int contentLength = 0;
        String cl = req.headers.get("Content-Length");
        if (cl == null) cl = req.headers.get("content-length");
        if (cl != null) {
            try {
                contentLength = Integer.parseInt(cl.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (contentLength > 0) {
            // 因为 reader 用了 ISO-8859-1 buffer，会缓存字节
            // 这里继续从 reader 读，按 char 读 contentLength 个，再编码
            char[] cbuf = new char[contentLength];
            int read = 0;
            while (read < contentLength) {
                int r = reader.read(cbuf, read, contentLength - read);
                if (r < 0) break;
                read += r;
            }
            // ISO-8859-1 → bytes 一一对应
            byte[] raw = new byte[read];
            for (int i = 0; i < read; i++) {
                raw[i] = (byte) (cbuf[i] & 0xff);
            }
            // body 实际上用 UTF-8（MCP 是 JSON）
            req.bodyBytes = raw;
        } else {
            req.bodyBytes = new byte[0];
        }
        return req;
    }

    private void decodeQuery(String qs, Map<String, String> out) {
        if (qs == null || qs.isEmpty()) return;
        for (String kv : qs.split("&")) {
            if (kv.isEmpty()) continue;
            int eq = kv.indexOf('=');
            String k = eq >= 0 ? kv.substring(0, eq) : kv;
            String v = eq >= 0 ? kv.substring(eq + 1) : "";
            try {
                k = URLDecoder.decode(k, "UTF-8");
                v = URLDecoder.decode(v, "UTF-8");
            } catch (Exception ignored) {
            }
            out.put(k, v);
        }
    }

    private JSONObject parseJsonBody(HttpRequest req) {
        try {
            byte[] body = req.bodyBytes;
            if (body == null || body.length == 0) return null;
            String text = new String(body, StandardCharsets.UTF_8);
            if (text.trim().isEmpty()) return null;
            Object parsed = JSON.parse(text);
            if (parsed instanceof JSONObject) {
                return (JSONObject) parsed;
            }
            // 暂不支持批量 batch 数组
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private void writeSimple(Socket sock, int code, String desc, String body, String ct) {
        try {
            OutputStream out = sock.getOutputStream();
            byte[] data = body == null ? new byte[0]
                    : body.getBytes(StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            sb.append("HTTP/1.1 ").append(code).append(" ").append(desc).append("\r\n");
            sb.append("Content-Type: ").append(ct).append("; charset=utf-8\r\n");
            sb.append("Content-Length: ").append(data.length).append("\r\n");
            appendCommonHeaders(sb);
            sb.append("Connection: close\r\n\r\n");
            out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
            if (data.length > 0) {
                out.write(data);
            }
            out.flush();
        } catch (IOException ignored) {
        }
    }

    private void appendCommonHeaders(StringBuilder sb) {
        sb.append("Server: jar-analyzer-mcp\r\n");
        sb.append("Access-Control-Allow-Origin: *\r\n");
    }

    private void safeClose(Socket sock) {
        try {
            if (sock != null) sock.close();
        } catch (IOException ignored) {
        }
    }

    // ------------------------------------------------------------
    // EVENT NOTIFY
    // ------------------------------------------------------------
    private void log(String msg) {
        if (config.isDebug()) {
            logger.info(msg);
        }
        if (listener != null) {
            try {
                listener.onLog(msg);
            } catch (Throwable ignored) {
            }
        }
    }

    private void warn(String msg) {
        logger.warn(msg);
        if (listener != null) {
            try {
                listener.onWarn(msg);
            } catch (Throwable ignored) {
            }
        }
    }

    private void requestLog(String transport, String method, boolean ok) {
        if (listener != null) {
            try {
                listener.onRequest(transport, method, ok);
            } catch (Throwable ignored) {
            }
        }
        if (config.isDebug()) {
            logger.info("MCP {} {} -> {}", transport, method, ok ? "ok" : "err");
        }
    }

    private void notifyConnections() {
        if (listener != null) {
            try {
                listener.onConnectionChanged(sseSessions.size(), streamableActive.get());
            } catch (Throwable ignored) {
            }
        }
    }

    // ------------------------------------------------------------
    // INTERNAL DTO
    // ------------------------------------------------------------
    private static class HttpRequest {
        String method = "GET";
        String path = "/";
        final Map<String, String> headers = new LinkedHashMap<>();
        final Map<String, String> query = new LinkedHashMap<>();
        byte[] bodyBytes = new byte[0];
    }

    /**
     * 仅供测试场景统计响应大小
     */
    @SuppressWarnings("unused")
    private static int sizeOfBaos(ByteArrayOutputStream b) {
        return b == null ? 0 : b.size();
    }
}
