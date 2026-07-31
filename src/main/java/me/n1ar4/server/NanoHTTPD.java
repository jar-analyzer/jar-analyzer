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

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class NanoHTTPD {
    public static final int SOCKET_READ_TIMEOUT = 5000;
    public static final int MAX_REQUEST_BODY_SIZE = 1024 * 1024;
    public static final int MAX_REQUEST_LINE_SIZE = 8192;
    public static final int MAX_HEADER_LINE_SIZE = 8192;
    public static final int MAX_HEADER_SIZE = 32768;
    public static final int MAX_HEADER_COUNT = 100;
    public static final int MAX_PARAMETER_COUNT = 256;
    public static final int MAX_CONNECTIONS = 16;
    public static final int MAX_QUEUED_CONNECTIONS = 32;

    private final String hostname;
    private final int port;
    private final Set<Socket> openSockets =
            Collections.newSetFromMap(new ConcurrentHashMap<Socket, Boolean>());
    private ServerSocket serverSocket;
    private Thread serverThread;
    private ThreadPoolExecutor workers;
    private int readTimeout = SOCKET_READ_TIMEOUT;

    public NanoHTTPD(String hostname, int port) {
        this.hostname = hostname == null || hostname.isEmpty() ? "0.0.0.0" : hostname;
        this.port = port;
    }

    public synchronized void start(int timeout, boolean daemon) throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            throw new IllegalStateException("HTTP server is already running");
        }
        this.readTimeout = timeout > 0 ? timeout : SOCKET_READ_TIMEOUT;
        AtomicInteger workerId = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable,
                    "nanohttpd-worker-" + workerId.incrementAndGet());
            thread.setDaemon(daemon);
            return thread;
        };
        this.workers = new ThreadPoolExecutor(
                MAX_CONNECTIONS,
                MAX_CONNECTIONS,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(MAX_QUEUED_CONNECTIONS),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
        this.serverSocket = new ServerSocket();
        try {
            this.serverSocket.bind(
                    new InetSocketAddress(hostname, port),
                    MAX_QUEUED_CONNECTIONS);
        } catch (IOException e) {
            this.workers.shutdownNow();
            this.serverSocket.close();
            throw e;
        }
        this.serverThread = new Thread(this::acceptLoop, "nanohttpd-acceptor");
        this.serverThread.setDaemon(daemon);
        this.serverThread.start();
    }

    public synchronized void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
        for (Socket socket : openSockets) {
            closeQuietly(socket);
        }
        openSockets.clear();
        if (workers != null) {
            for (Runnable task : workers.shutdownNow()) {
                if (task instanceof ConnectionTask) {
                    ((ConnectionTask) task).close();
                }
            }
        }
    }

    public int getListeningPort() {
        ServerSocket socket = serverSocket;
        return socket == null ? port : socket.getLocalPort();
    }

    protected int getWorkerCount() {
        ThreadPoolExecutor executor = workers;
        return executor == null ? 0 : executor.getPoolSize();
    }

    protected int getQueuedConnectionCount() {
        ThreadPoolExecutor executor = workers;
        return executor == null ? 0 : executor.getQueue().size();
    }

    private void acceptLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(readTimeout);
                openSockets.add(socket);
                try {
                    workers.execute(new ConnectionTask(socket));
                } catch (RejectedExecutionException e) {
                    openSockets.remove(socket);
                    closeQuietly(socket);
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    private void handle(Socket socket) {
        try (Socket s = socket) {
            OutputStream out = s.getOutputStream();
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(readTimeout);
            InputStream in = new BufferedInputStream(
                    new DeadlineInputStream(s.getInputStream(), s, deadline));
            try {
                processRequest(s, in, out);
            } catch (HttpException e) {
                writeResponse(out, newFixedLengthResponse(
                        e.status, "text/plain", e.getMessage()));
            }
        } catch (IOException ignored) {
        } finally {
            openSockets.remove(socket);
        }
    }

    private void processRequest(Socket socket, InputStream in, OutputStream out)
            throws IOException, HttpException {
        int[] headerBytes = {0};
        String requestLine = readLine(in, MAX_REQUEST_LINE_SIZE,
                headerBytes, Response.Status.BAD_REQUEST);
        if (requestLine == null || requestLine.isEmpty()) {
            return;
        }

        String[] parts = requestLine.split("\\s+");
        if (parts.length != 3
                || !("HTTP/1.0".equals(parts[2])
                || "HTTP/1.1".equals(parts[2]))) {
            throw new HttpException(
                    Response.Status.BAD_REQUEST, "Malformed request line");
        }
        String method = parts[0];
        String fullUri = parts[1];
        String uri = fullUri;
        String query = "";
        int q = fullUri.indexOf('?');
        if (q >= 0) {
            uri = fullUri.substring(0, q);
            query = fullUri.substring(q + 1);
        }

        Map<String, String> headers =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        boolean contentLengthSeen = false;
        int headerCount = 0;
        String line;
        while ((line = readLine(in, MAX_HEADER_LINE_SIZE, headerBytes,
                Response.Status.REQUEST_HEADER_FIELDS_TOO_LARGE)) != null) {
            if (line.isEmpty()) break;
            if (++headerCount > MAX_HEADER_COUNT) {
                throw new HttpException(
                        Response.Status.REQUEST_HEADER_FIELDS_TOO_LARGE,
                        "Too many request headers");
            }
            int idx = line.indexOf(':');
            if (idx <= 0 || Character.isWhitespace(line.charAt(0))) {
                throw new HttpException(
                        Response.Status.BAD_REQUEST, "Malformed header");
            }
            String key = line.substring(0, idx);
            String val = line.substring(idx + 1).trim();
            if (!isValidHeaderName(key)) {
                throw new HttpException(
                        Response.Status.BAD_REQUEST, "Malformed header name");
            }
            if (!isValidHeaderValue(val)) {
                throw new HttpException(
                        Response.Status.BAD_REQUEST, "Malformed header value");
            }
            if ("Content-Length".equalsIgnoreCase(key)) {
                if (contentLengthSeen) {
                    throw new HttpException(
                            Response.Status.BAD_REQUEST,
                            "Duplicate Content-Length");
                }
                contentLengthSeen = true;
            }
            headers.put(key, val);
        }
        if (line == null) {
            throw new HttpException(
                    Response.Status.BAD_REQUEST, "Incomplete headers");
        }

        if (headers.containsKey("Transfer-Encoding")) {
            throw new HttpException(
                    Response.Status.BAD_REQUEST,
                    "Transfer-Encoding is not supported");
        }

        String cl = headers.get("Content-Length");
        if (cl == null && requiresContentLength(method)) {
            throw new HttpException(
                    Response.Status.LENGTH_REQUIRED,
                    "Content-Length is required");
        }

        int contentLength = 0;
        if (cl != null) {
            if (cl.isEmpty() || !isDecimal(cl)) {
                throw new HttpException(
                        Response.Status.BAD_REQUEST,
                        "Invalid Content-Length");
            }
            long parsedLength;
            try {
                parsedLength = Long.parseLong(cl);
            } catch (NumberFormatException e) {
                throw new HttpException(
                        Response.Status.BAD_REQUEST,
                        "Invalid Content-Length");
            }
            if (parsedLength > MAX_REQUEST_BODY_SIZE) {
                throw new HttpException(
                        Response.Status.PAYLOAD_TOO_LARGE,
                        "Request body is too large");
            }
            contentLength = (int) parsedLength;
        }

        byte[] body = new byte[contentLength];
        if (contentLength > 0) {
            readFully(in, body);
        }

        Map<String, List<String>> parameters = new LinkedHashMap<>();
        int parameterCount = 0;
        if (!query.isEmpty()) {
            parameterCount = decodeParams(query, parameters, parameterCount);
        }
        String ctype = headers.getOrDefault("Content-Type", "");
        if ("POST".equalsIgnoreCase(method)
                && ctype.toLowerCase(Locale.ROOT)
                .startsWith("application/x-www-form-urlencoded")) {
            String post = new String(body, StandardCharsets.UTF_8);
            decodeParams(post, parameters, parameterCount);
        }

        HTTPSession session = new HTTPSession(uri, method, headers, parameters,
                socket.getInetAddress() != null
                        ? socket.getInetAddress().getHostAddress()
                        : "127.0.0.1",
                in);

        Response resp = serve(session);
        if (resp == null) {
            resp = newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR, "text/plain", "");
        }
        writeResponse(out, resp);
    }

    private static String readLine(InputStream in, int maxLineBytes,
                                   int[] totalBytes,
                                   Response.Status tooLongStatus)
            throws IOException, HttpException {
        StringBuilder line = new StringBuilder(Math.min(128, maxLineBytes));
        while (true) {
            int value = in.read();
            if (value < 0) {
                return null;
            }
            totalBytes[0]++;
            if (totalBytes[0] > MAX_HEADER_SIZE) {
                throw new HttpException(
                        Response.Status.REQUEST_HEADER_FIELDS_TOO_LARGE,
                        "Request headers are too large");
            }
            if (value == '\r') {
                int next = in.read();
                if (next < 0) {
                    return null;
                }
                totalBytes[0]++;
                if (totalBytes[0] > MAX_HEADER_SIZE) {
                    throw new HttpException(
                            Response.Status.REQUEST_HEADER_FIELDS_TOO_LARGE,
                            "Request headers are too large");
                }
                if (next != '\n') {
                    throw new HttpException(
                            Response.Status.BAD_REQUEST,
                            "Header lines must end with CRLF");
                }
                return line.toString();
            }
            if (value == '\n') {
                throw new HttpException(
                        Response.Status.BAD_REQUEST,
                        "Header lines must end with CRLF");
            }
            if (line.length() >= maxLineBytes) {
                throw new HttpException(tooLongStatus, "HTTP line is too large");
            }
            line.append((char) (value & 0xff));
        }
    }

    private static void readFully(InputStream in, byte[] body)
            throws IOException, HttpException {
        int offset = 0;
        while (offset < body.length) {
            int read = in.read(body, offset, body.length - offset);
            if (read < 0) {
                throw new HttpException(
                        Response.Status.BAD_REQUEST,
                        "Incomplete request body");
            }
            offset += read;
        }
    }

    private static boolean requiresContentLength(String method) {
        return "POST".equalsIgnoreCase(method)
                || "PUT".equalsIgnoreCase(method)
                || "PATCH".equalsIgnoreCase(method);
    }

    private static boolean isDecimal(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidHeaderName(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean valid = c >= 'a' && c <= 'z'
                    || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9'
                    || "!#$%&'*+-.^_`|~".indexOf(c) >= 0;
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidHeaderValue(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == 0x7f || (c < 0x20 && c != '\t')) {
                return false;
            }
        }
        return true;
    }

    private static void closeQuietly(Socket socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private int decodeParams(String qs, Map<String, List<String>> parameters,
                             int parameterCount) throws HttpException {
        int start = 0;
        while (start <= qs.length()) {
            int end = qs.indexOf('&', start);
            if (end < 0) {
                end = qs.length();
            }
            String pair = qs.substring(start, end);
            if (!pair.isEmpty()) {
                if (++parameterCount > MAX_PARAMETER_COUNT) {
                    throw new HttpException(
                            Response.Status.PAYLOAD_TOO_LARGE,
                            "Too many request parameters");
                }
                int idx = pair.indexOf('=');
                String key = idx >= 0 ? pair.substring(0, idx) : pair;
                String val = idx >= 0 ? pair.substring(idx + 1) : "";
                try {
                    key = URLDecoder.decode(key, "UTF-8");
                    val = URLDecoder.decode(val, "UTF-8");
                } catch (Exception ignored) {
                }
                parameters.computeIfAbsent(
                        key, k -> new ArrayList<>()).add(val);
            }
            if (end == qs.length()) {
                break;
            }
            start = end + 1;
        }
        return parameterCount;
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
            BAD_REQUEST(400, "Bad Request"),
            LENGTH_REQUIRED(411, "Length Required"),
            PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
            REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
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

    private final class ConnectionTask implements Runnable {
        private final Socket socket;

        private ConnectionTask(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            handle(socket);
        }

        private void close() {
            openSockets.remove(socket);
            closeQuietly(socket);
        }
    }

    private static final class DeadlineInputStream extends FilterInputStream {
        private final Socket socket;
        private final long deadline;

        private DeadlineInputStream(InputStream in, Socket socket, long deadline) {
            super(in);
            this.socket = socket;
            this.deadline = deadline;
        }

        @Override
        public int read() throws IOException {
            updateTimeout();
            return super.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            updateTimeout();
            return super.read(b, off, len);
        }

        private void updateTimeout() throws IOException {
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos <= 0) {
                throw new SocketTimeoutException("HTTP request read timed out");
            }
            long remainingMillis = TimeUnit.NANOSECONDS.toMillis(remainingNanos);
            socket.setSoTimeout((int) Math.max(1L, remainingMillis));
        }
    }

    private static final class HttpException extends Exception {
        private final Response.Status status;

        private HttpException(Response.Status status, String message) {
            super(message);
            this.status = status;
        }
    }
}
