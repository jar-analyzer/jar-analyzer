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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * SSE 单个会话
 * 持有一个长连接 socket，向客户端推送 event-stream
 * 同时维护当前 sessionId 用于 POST /message?sessionId=xxx 路由
 * <p>
 * 注意:
 * 1) 任意一次写失败会立刻 close() 自身，调用方需检查 isClosed()
 * 2) 维护一个 inflight 表，记录该会话上正在执行的 tools/call Future
 * 用于 notifications/cancelled 的精确取消，避免线程僵死
 */
public class SseSession {
    private final String sessionId;
    private final Socket socket;
    private final OutputStream out;
    private volatile boolean closed = false;
    private final long createdAt = System.currentTimeMillis();
    private volatile String remote = "";

    /**
     * 该会话上正在执行的 tools/call Future
     * key = JSON-RPC requestId 的 String 形式
     */
    private final ConcurrentHashMap<String, Future<?>> inflight = new ConcurrentHashMap<>();

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

    public void registerInflight(String requestId, Future<?> f) {
        if (requestId == null || f == null) return;
        inflight.put(requestId, f);
    }

    public void unregisterInflight(String requestId) {
        if (requestId == null) return;
        inflight.remove(requestId);
    }

    /**
     * 取消指定 requestId 对应的工具调用
     *
     * @return 是否真的取消了一个 future
     */
    public boolean cancelInflight(String requestId) {
        if (requestId == null) return false;
        Future<?> f = inflight.remove(requestId);
        if (f == null) return false;
        return f.cancel(true);
    }

    /**
     * 写一条具名 SSE 事件
     * 写失败立即关闭会话，避免半开连接拖垮整个推送通道
     */
    public synchronized void sendEvent(String event, String data) throws IOException {
        if (closed) throw new IOException("session closed");
        try {
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
        } catch (IOException ioe) {
            close();
            throw ioe;
        }
    }

    /**
     * 发送注释行（保活）: ":\n\n"
     * 写失败立即关闭会话
     */
    public synchronized void sendComment(String comment) throws IOException {
        if (closed) throw new IOException("session closed");
        try {
            out.write(((comment == null ? ":" : ":" + comment) + "\n\n")
                    .getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException ioe) {
            close();
            throw ioe;
        }
    }

    public synchronized void close() {
        if (closed) return;
        closed = true;
        // 取消所有未完成任务
        for (Future<?> f : inflight.values()) {
            try {
                f.cancel(true);
            } catch (Throwable ignored) {
            }
        }
        inflight.clear();
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
