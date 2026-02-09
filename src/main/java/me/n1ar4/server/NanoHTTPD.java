/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.server;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class NanoHTTPD {
    public static final int SOCKET_READ_TIMEOUT = 5000;

    private final String hostname;
    private final int port;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private int readTimeout = SOCKET_READ_TIMEOUT;

    public NanoHTTPD(String hostname, int port) {
        this.hostname = hostname == null || hostname.isEmpty() ? "0.0.0.0" : hostname;
        this.port = port;
    }

    public void start(int timeout, boolean daemon) throws IOException {
        this.readTimeout = timeout;
        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(new InetSocketAddress(hostname, port));
        this.serverThread = new Thread(this::acceptLoop);
        this.serverThread.setDaemon(daemon);
        this.serverThread.start();
    }

    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    private void acceptLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(readTimeout);
                new Thread(() -> handle(socket)).start();
            } catch (IOException e) {
                break;
            }
        }
    }

    private void handle(Socket socket) {
        try (Socket s = socket;
             InputStream in = s.getInputStream();
             OutputStream out = s.getOutputStream()) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.ISO_8859_1));
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] parts = requestLine.split("\\s+");
            String method = parts.length > 0 ? parts[0] : "GET";
            String fullUri = parts.length > 1 ? parts[1] : "/";
            String uri = fullUri;
            String query = "";
            int q = fullUri.indexOf('?');
            if (q >= 0) {
                uri = fullUri.substring(0, q);
                query = fullUri.substring(q + 1);
            }

            Map<String, String> headers = new LinkedHashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) break;
                int idx = line.indexOf(':');
                if (idx > 0) {
                    String key = line.substring(0, idx).trim();
                    String val = line.substring(idx + 1).trim();
                    headers.put(key, val);
                }
            }

            byte[] body = new byte[0];
            int contentLength = 0;
            String cl = headers.get("Content-Length");
            if (cl != null) {
                try {
                    contentLength = Integer.parseInt(cl);
                } catch (NumberFormatException ignored) {
                }
            }
            if (contentLength > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(contentLength);
                int remaining = contentLength;
                byte[] buf = new byte[8192];
                while (remaining > 0) {
                    int r = in.read(buf, 0, Math.min(buf.length, remaining));
                    if (r < 0) break;
                    bos.write(buf, 0, r);
                    remaining -= r;
                }
                body = bos.toByteArray();
            }

            Map<String, List<String>> parameters = new LinkedHashMap<>();
            if (!query.isEmpty()) {
                decodeParams(query, parameters);
            }
            String ctype = headers.getOrDefault("Content-Type", "");
            if ("POST".equalsIgnoreCase(method) && ctype.toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded")) {
                String post = new String(body, StandardCharsets.UTF_8);
                decodeParams(post, parameters);
            }

            HTTPSession session = new HTTPSession(uri, method, headers, parameters,
                    s.getInetAddress() != null ? s.getInetAddress().getHostAddress() : "127.0.0.1",
                    in);

            Response resp = serve(session);
            if (resp == null) {
                resp = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "");
            }
            writeResponse(out, resp);
        } catch (IOException ignored) {
        }
    }

    private void decodeParams(String qs, Map<String, List<String>> parameters) {
        String[] pairs = qs.split("&");
        for (String pair : pairs) {
            if (pair.isEmpty()) continue;
            int idx = pair.indexOf('=');
            String key = idx >= 0 ? pair.substring(0, idx) : pair;
            String val = idx >= 0 ? pair.substring(idx + 1) : "";
            try {
                key = URLDecoder.decode(key, "UTF-8");
                val = URLDecoder.decode(val, "UTF-8");
            } catch (Exception ignored) {
            }
            parameters.computeIfAbsent(key, k -> new ArrayList<>()).add(val);
        }
    }

    private void writeResponse(OutputStream out, Response resp) throws IOException {
        byte[] data;
        if (resp.data != null) {
            data = resp.data;
        } else if (resp.stream != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int r;
            while ((r = resp.stream.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            data = bos.toByteArray();
        } else {
            data = new byte[0];
        }

        StringBuilder sb = new StringBuilder();
        sb.append("HTTP/1.1 ").append(resp.status.code).append(" ").append(resp.status.desc).append("\r\n");
        sb.append("Content-Type: ").append(resp.mimeType != null ? resp.mimeType : "text/plain").append("\r\n");
        sb.append("Content-Length: ").append(data.length).append("\r\n");
        for (Map.Entry<String, String> e : resp.headers.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        sb.append("Connection: close\r\n");
        sb.append("\r\n");
        out.write(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        out.write(data);
        out.flush();
    }

    public static Response newFixedLengthResponse(Response.Status status, String mimeType, String text) {
        byte[] data = text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
        return new Response(status, mimeType, data);
    }

    public static Response newChunkedResponse(Response.Status status, String mimeType, InputStream data) {
        return new Response(status, mimeType, data);
    }

    public abstract Response serve(IHTTPSession session);

    public interface IHTTPSession {
        String getUri();

        String getMethod();

        Map<String, String> getHeaders();

        Map<String, List<String>> getParameters();

        String getRemoteIpAddress();

        InputStream getInputStream();
    }

    public static class Response {
        public enum Status {
            OK(200, "OK"),
            INTERNAL_ERROR(500, "Internal Server Error");
            public final int code;
            public final String desc;

            Status(int code, String desc) {
                this.code = code;
                this.desc = desc;
            }
        }

        private final Status status;
        private final String mimeType;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final byte[] data;
        private final InputStream stream;

        public Response(Status status, String mimeType, byte[] data) {
            this.status = status;
            this.mimeType = mimeType;
            this.data = data;
            this.stream = null;
        }

        public Response(Status status, String mimeType, InputStream stream) {
            this.status = status;
            this.mimeType = mimeType;
            this.stream = stream;
            this.data = null;
        }

        public void addHeader(String key, String value) {
            headers.put(key, value);
        }
    }

    private static class HTTPSession implements IHTTPSession {
        private final String uri;
        private final String method;
        private final Map<String, String> headers;
        private final Map<String, List<String>> parameters;
        private final String remoteIp;
        private final InputStream inputStream;

        HTTPSession(String uri, String method, Map<String, String> headers,
                    Map<String, List<String>> parameters, String remoteIp, InputStream inputStream) {
            this.uri = uri;
            this.method = method;
            this.headers = headers;
            this.parameters = parameters;
            this.remoteIp = remoteIp;
            this.inputStream = inputStream;
        }

        public String getUri() {
            return uri;
        }

        public String getMethod() {
            return method;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Map<String, List<String>> getParameters() {
            return parameters;
        }

        public String getRemoteIpAddress() {
            return remoteIp;
        }

        public InputStream getInputStream() {
            return inputStream;
        }
    }
}
