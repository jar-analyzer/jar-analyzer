/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.mcp.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * SSE 单个会话
 * 持有一个长连接 socket，向客户端推送 event-stream
 * 同时维护当前 sessionId 用于 POST /message?sessionId=xxx 路由
 */
public class SseSession {
    private final String sessionId;
    private final Socket socket;
    private final OutputStream out;
    private volatile boolean closed = false;
    private final long createdAt = System.currentTimeMillis();
    private volatile String remote = "";

    public SseSession(Socket socket, OutputStream out) {
        this.sessionId = UUID.randomUUID().toString().replace("-", "");
        this.socket = socket;
        this.out = out;
        if (socket != null && socket.getInetAddress() != null) {
            this.remote = socket.getInetAddress().getHostAddress();
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isClosed() {
        return closed;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getRemote() {
        return remote;
    }

    /**
     * 写一条具名 SSE 事件
     */
    public synchronized void sendEvent(String event, String data) throws IOException {
        if (closed) throw new IOException("session closed");
        StringBuilder sb = new StringBuilder();
        if (event != null && !event.isEmpty()) {
            sb.append("event: ").append(event).append("\n");
        }
        // 多行 data 需要拆分
        if (data != null) {
            for (String line : data.split("\n", -1)) {
                sb.append("data: ").append(line).append("\n");
            }
        } else {
            sb.append("data: \n");
        }
        sb.append("\n");
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * 发送注释行（保活）: ":\n\n"
     */
    public synchronized void sendComment(String comment) throws IOException {
        if (closed) throw new IOException("session closed");
        out.write(((comment == null ? ":" : ":" + comment) + "\n\n")
                .getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public synchronized void close() {
        if (closed) return;
        closed = true;
        try {
            if (out != null) out.close();
        } catch (Throwable ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (Throwable ignored) {
        }
    }
}
