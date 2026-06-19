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
import me.n1ar4.jar.analyzer.mcp.protocol.JsonRpc;
import me.n1ar4.jar.analyzer.mcp.protocol.McpMethods;
import me.n1ar4.jar.analyzer.mcp.transport.SseSession;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
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
    /**
     * 单独跑 tools/call 的线程池（有界 + 拒绝即返 busy），便于控并发与精确取消
     * 与 HTTP IO 处理用的 workers 分离，避免阻塞路由
     */
    private final ThreadPoolExecutor toolExecutor;
    /**
     * SSE 心跳调度器：所有 SSE 长连接共享一个 ScheduledExecutorService，
     * 不再为每个连接占用一个 worker 线程，避免长跑下 workers 池无限膨胀
     */
    private final ScheduledExecutorService sseHeartbeatExecutor;
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running = false;

    // SSE 会话表
    private final Map<String, SseSession> sseSessions = new ConcurrentHashMap<>();
    // Streamable HTTP 当前活跃流响应数
    private final AtomicInteger streamableActive = new AtomicInteger(0);

    /**
     * 全局正在执行的 tools/call（覆盖所有 transport），key = JSON-RPC requestId 字符串
     * 用于 notifications/cancelled 的精确取消（包括 Streamable HTTP，因取消通知是另一条独立 POST）
     */
    private final Map<String, Future<?>> pendingRequests = new ConcurrentHashMap<>();

    // 事件监听
    private volatile McpEventListener listener;

    public McpServer(McpConfig config) {
        this.config = config;
        this.workers = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "mcp-worker");
            t.setDaemon(true);
            return t;
        });
        int conc = Math.max(1, config.getToolMaxConcurrency());
        int qcap = Math.max(1, config.getToolQueueCapacity());
        this.toolExecutor = new ThreadPoolExecutor(
                conc, conc,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(qcap),
                r -> {
                    Thread t = new Thread(r, "mcp-tool");
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.AbortPolicy());
        this.toolExecutor.allowCoreThreadTimeOut(true);
        // 心跳用一个固定容量的调度池即可（每个 schedule 任务只是写一行 ping）
        this.sseHeartbeatExecutor = Executors.newScheduledThreadPool(
                Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors())),
                r -> {
                    Thread t = new Thread(r, "mcp-sse-heartbeat");
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
        // 取消所有 pending 工具调用
        for (Future<?> f : pendingRequests.values()) {
            try {
                f.cancel(true);
            } catch (Throwable ignored) {
            }
        }
        pendingRequests.clear();
        notifyConnections();
        try {
            workers.shutdownNow();
        } catch (Throwable ignored) {
        }
        try {
            toolExecutor.shutdownNow();
        } catch (Throwable ignored) {
        }
        try {
            sseHeartbeatExecutor.shutdownNow();
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
        // 容忍 accept 偶发异常（FD 暂时耗尽、临时网络抖动等），不要让单次失败拖垮整个 server
        // 仅当 serverSocket 已关闭时退出
        while (running) {
            final Socket sock;
            try {
                sock = serverSocket.accept();
            } catch (IOException e) {
                if (!running || serverSocket == null || serverSocket.isClosed()) {
                    break; // 正常停服路径
                }
                warn("accept error (will retry): " + e.getMessage());
                // 短暂退避，避免在持续异常时占满 CPU
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            } catch (Throwable t) {
                // 其它意外（OOME/SecurityException 等）也兜住
                if (!running) break;
                warn("accept fatal (will retry): " + t.getClass().getSimpleName()
                        + ": " + t.getMessage());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }
            try {
                workers.submit(() -> handleSocket(sock));
            } catch (RejectedExecutionException ree) {
                // workers 池关闭中，安全关闭新连接
                safeClose(sock);
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
        // 长连接：读不超时；写交给 socket 自身缓冲。
        // 通过定时心跳 + sendComment 写失败立即关会话来识别半开连接
        sock.setSoTimeout(0);
        // 关闭 Nagle，让 SSE 事件尽快到达
        try {
            sock.setTcpNoDelay(true);
        } catch (Throwable ignored) {
        }
        try {
            sock.setKeepAlive(true);
        } catch (Throwable ignored) {
        }
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

        final SseSession session = new SseSession(sock, out);
        sseSessions.put(session.getSessionId(), session);
        notifyConnections();
        log("SSE session opened: " + session.getSessionId() + " from " + session.getRemote());

        // 推送 endpoint 事件 - 告诉客户端 POST 到何处
        try {
            String endpoint = "/message?sessionId=" + session.getSessionId();
            session.sendEvent("endpoint", endpoint);
        } catch (IOException ioe) {
            // 第一次写就失败：直接清理，不调度心跳
            sseSessions.remove(session.getSessionId());
            session.close();
            notifyConnections();
            log("SSE session closed (initial write failed): " + session.getSessionId());
            return;
        }

        // 调度周期性心跳，不再阻塞 worker 线程
        // 注意：这里不需要把心跳 future 单独保存，session.close() 会触发 sendComment 抛异常自动取消
        final int heartbeatSec = Math.max(2, config.getSseHeartbeatSec());
        final ScheduledFuture<?>[] holder = new ScheduledFuture<?>[1];
        Runnable beat = () -> {
            if (!running || session.isClosed()) {
                if (holder[0] != null) holder[0].cancel(false);
                cleanupSseSession(session);
                return;
            }
            try {
                session.sendComment("ping");
            } catch (IOException ioe) {
                // sendComment 内部已 close 了 session
                if (holder[0] != null) holder[0].cancel(false);
                cleanupSseSession(session);
            } catch (Throwable t) {
                if (holder[0] != null) holder[0].cancel(false);
                cleanupSseSession(session);
            }
        };
        try {
            holder[0] = sseHeartbeatExecutor.scheduleWithFixedDelay(
                    beat, heartbeatSec, heartbeatSec, TimeUnit.SECONDS);
        } catch (RejectedExecutionException ree) {
            // 调度器已关闭：直接清理
            cleanupSseSession(session);
        }
        // handleSseGet 立即返回，worker 线程被释放
    }

    private void cleanupSseSession(SseSession session) {
        if (session == null) return;
        if (sseSessions.remove(session.getSessionId()) != null) {
            session.close();
            notifyConnections();
            log("SSE session closed: " + session.getSessionId());
        } else {
            session.close();
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

        // 通知类消息（无 id）单独处理
        if (JsonRpc.isNotification(msg)) {
            handleNotification("sse", session, msg);
            return;
        }

        final String mname = msg.getString("method");

        // tools/call 走带超时和取消的执行路径
        if ("tools/call".equals(mname)) {
            dispatchToolCallAsync(session, msg);
            return;
        }

        // 其它请求（initialize / tools/list / ping ...）走 IO worker，快速响应
        workers.submit(() -> {
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

    /**
     * 处理 JSON-RPC 通知（无 id），不产生响应、不计入 request 统计
     * 当前重点支持: notifications/cancelled
     */
    private void handleNotification(String transport, SseSession session, JSONObject msg) {
        String method = msg.getString("method");
        if ("notifications/cancelled".equals(method)) {
            JSONObject params = msg.getJSONObject("params");
            String reqId = params == null ? null
                    : (params.get("requestId") == null ? null : String.valueOf(params.get("requestId")));
            String reason = params == null ? null : params.getString("reason");
            boolean cancelled = false;
            if (reqId != null) {
                // 优先查 SSE 会话，命中失败再查全局表（Streamable HTTP 走全局）
                if (session != null) {
                    cancelled = session.cancelInflight(reqId);
                }
                if (!cancelled) {
                    Future<?> f = pendingRequests.remove(reqId);
                    if (f != null) {
                        cancelled = f.cancel(true);
                    }
                }
            }
            if (config.isDebug()) {
                logger.info("MCP {} cancelled requestId={} reason={} effective={}",
                        transport, reqId, reason, cancelled);
            }
            return;
        }
        // notifications/initialized 等其它通知静默处理
        if (config.isDebug()) {
            logger.info("MCP {} notification: {}", transport, method);
        }
        // 仍然让 dispatch 走一遍，以兼容未来扩展（其内部会返回 null）
        try {
            McpMethods.dispatch(msg);
        } catch (Throwable ignored) {
        }
    }

    /**
     * 异步执行 tools/call，带超时与可取消能力
     * 失败/超时也以 "isError + content" 的标准 tool 结果返回，避免 Agent 端等到协议层超时
     */
    private void dispatchToolCallAsync(final SseSession session, final JSONObject msg) {
        final Object id = msg.get("id");
        final String reqId = id == null ? null : String.valueOf(id);
        final String mname = msg.getString("method");
        final int timeoutSec = config.getToolCallTimeoutSec();

        final Future<JSONObject> future;
        try {
            future = toolExecutor.submit(() -> {
                long timeoutMs = (long) Math.max(0, timeoutSec) * 1000L;
                McpContext.enter(timeoutMs);
                try {
                    return McpMethods.dispatch(msg);
                } finally {
                    McpContext.leave();
                }
            });
        } catch (RejectedExecutionException ree) {
            // tool 池已满，立刻给出 busy 错误，避免 Agent 端干等
            warn("tools/call rejected (busy), requestId=" + reqId);
            JSONObject busy = McpMethods.buildToolResult(id,
                    "server busy: tool executor saturated, please retry later", true);
            requestLog("sse", mname, false);
            try {
                session.sendEvent("message", busy.toJSONString());
            } catch (IOException e) {
                warn("sse push failed: " + e.getMessage());
            }
            return;
        }

        if (reqId != null) {
            session.registerInflight(reqId, future);
            pendingRequests.put(reqId, future);
        }

        // 用 workers 起一个等待任务，避免阻塞 toolExecutor 自身
        workers.submit(() -> {
            JSONObject resp = null;
            boolean ok = false;
            boolean cancelled = false;
            try {
                if (timeoutSec > 0) {
                    resp = future.get(timeoutSec, TimeUnit.SECONDS);
                } else {
                    resp = future.get();
                }
                ok = resp != null && resp.get("error") == null;
            } catch (TimeoutException te) {
                future.cancel(true);
                resp = McpMethods.buildToolResult(id,
                        "tool execution timeout after " + timeoutSec + "s, cancelled by server",
                        true);
                warn("tools/call timeout, requestId=" + reqId);
            } catch (java.util.concurrent.CancellationException ce) {
                cancelled = true;
                if (config.isDebug()) {
                    logger.info("tools/call cancelled, requestId={}", reqId);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                future.cancel(true);
                cancelled = true;
            } catch (Throwable ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                resp = McpMethods.buildToolResult(id,
                        "tool error: " + cause.getClass().getSimpleName()
                                + ": " + cause.getMessage(),
                        true);
            } finally {
                if (reqId != null) {
                    session.unregisterInflight(reqId);
                    pendingRequests.remove(reqId);
                }
            }
            if (cancelled) return; // 客户端已放弃，不再回复
            requestLog("sse", mname, ok);
            if (resp != null && !session.isClosed()) {
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
        // 通知：不响应、不计 request 统计
        if (JsonRpc.isNotification(msg)) {
            handleNotification("streamable", null, msg);
            writeSimple(sock, 202, "Accepted", "", "text/plain");
            safeClose(sock);
            return;
        }

        String mname = msg.getString("method");
        JSONObject resp = invokeWithTimeout(msg);
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
        try {
            sock.setTcpNoDelay(true);
        } catch (Throwable ignored) {
        }
        try {
            sock.setKeepAlive(true);
        } catch (Throwable ignored) {
        }
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
            // 通知：不计 request 统计、不响应
            if (JsonRpc.isNotification(msg)) {
                handleNotification("streamable", null, msg);
                return;
            }
            String mname = msg.getString("method");
            JSONObject resp = invokeWithTimeout(msg);
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

    /**
     * 同步调用 dispatch，对 tools/call 加超时；其它方法直接走 dispatch
     * 用于 streamable 同步分支
     * 关键点：把 future 注册到全局 pendingRequests，让 notifications/cancelled 也能取消 streamable 的请求
     */
    private JSONObject invokeWithTimeout(final JSONObject msg) {
        String mname = msg.getString("method");
        int timeoutSec = config.getToolCallTimeoutSec();
        if (!"tools/call".equals(mname) || timeoutSec <= 0) {
            return McpMethods.dispatch(msg);
        }
        final Object id = msg.get("id");
        final String reqId = id == null ? null : String.valueOf(id);

        final Future<JSONObject> future;
        try {
            future = toolExecutor.submit(() -> {
                long timeoutMs = (long) Math.max(0, timeoutSec) * 1000L;
                McpContext.enter(timeoutMs);
                try {
                    return McpMethods.dispatch(msg);
                } finally {
                    McpContext.leave();
                }
            });
        } catch (RejectedExecutionException ree) {
            warn("tools/call rejected (busy, streamable), id=" + id);
            return McpMethods.buildToolResult(id,
                    "server busy: tool executor saturated, please retry later", true);
        }
        if (reqId != null) {
            pendingRequests.put(reqId, future);
        }
        try {
            return future.get(timeoutSec, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            future.cancel(true);
            warn("tools/call timeout (streamable), id=" + id);
            return McpMethods.buildToolResult(id,
                    "tool execution timeout after " + timeoutSec + "s, cancelled by server",
                    true);
        } catch (java.util.concurrent.CancellationException ce) {
            // 客户端主动取消：仍然要给 HTTP 端一个回复（协议要求每个 request 都有 response）
            return McpMethods.buildToolResult(id,
                    "request cancelled by client", true);
        } catch (Throwable ex) {
            future.cancel(true);
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            return McpMethods.buildToolResult(id,
                    "tool error: " + cause.getClass().getSimpleName()
                            + ": " + cause.getMessage(),
                    true);
        } finally {
            if (reqId != null) {
                pendingRequests.remove(reqId);
            }
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
        // 防 OOM：拒绝过大的请求体
        int maxBody = config.getMaxBodyBytes();
        if (maxBody > 0 && contentLength > maxBody) {
            throw new IOException("request body too large: " + contentLength
                    + " > " + maxBody);
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
