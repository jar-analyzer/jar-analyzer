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

/**
 * MCP 服务端配置
 * 同时支持 SSE 与 Streamable HTTP 两种传输
 */
public class McpConfig {
    // MCP 监听地址
    private String bind = "127.0.0.1";
    // MCP 监听端口
    private int port = 20032;
    // 是否启用 token 校验
    private boolean auth = false;
    // token 值（请求需在 Header: Token 中携带）
    private String token = "JAR-ANALYZER-MCP-TOKEN";
    // 是否启用 SSE 传输（GET /sse + POST /message）
    private boolean enableSse = true;
    // 是否启用 Streamable HTTP 传输（POST/GET /mcp）
    private boolean enableStreamable = true;
    // 是否启用 DEBUG 日志
    private boolean debug = false;
    // 单次 tools/call 最大执行时长（秒），超时则取消并向客户端返回 isError
    // 设置为 <=0 表示不做服务端侧的超时限制
    private int toolCallTimeoutSec = 120;
    // SSE 心跳间隔（秒），同时也是 SSE 写超时上限（建议 5~15）
    private int sseHeartbeatSec = 10;
    // tools/call 工作线程池上限（高负载下避免线程数无限膨胀）
    private int toolMaxConcurrency = 16;
    // tools/call 排队队列上限，超出时直接 busy
    private int toolQueueCapacity = 64;
    // HTTP 请求体最大字节数（防 OOM，远端可控）
    private int maxBodyBytes = 8 * 1024 * 1024;

    public String getBind() {
        return bind;
    }

    public void setBind(String bind) {
        this.bind = bind;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isEnableSse() {
        return enableSse;
    }

    public void setEnableSse(boolean enableSse) {
        this.enableSse = enableSse;
    }

    public boolean isEnableStreamable() {
        return enableStreamable;
    }

    public void setEnableStreamable(boolean enableStreamable) {
        this.enableStreamable = enableStreamable;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getToolCallTimeoutSec() {
        return toolCallTimeoutSec;
    }

    public void setToolCallTimeoutSec(int toolCallTimeoutSec) {
        this.toolCallTimeoutSec = toolCallTimeoutSec;
    }

    public int getSseHeartbeatSec() {
        return sseHeartbeatSec;
    }

    public void setSseHeartbeatSec(int sseHeartbeatSec) {
        this.sseHeartbeatSec = sseHeartbeatSec;
    }

    public int getToolMaxConcurrency() {
        return toolMaxConcurrency;
    }

    public void setToolMaxConcurrency(int toolMaxConcurrency) {
        this.toolMaxConcurrency = toolMaxConcurrency;
    }

    public int getToolQueueCapacity() {
        return toolQueueCapacity;
    }

    public void setToolQueueCapacity(int toolQueueCapacity) {
        this.toolQueueCapacity = toolQueueCapacity;
    }

    public int getMaxBodyBytes() {
        return maxBodyBytes;
    }

    public void setMaxBodyBytes(int maxBodyBytes) {
        this.maxBodyBytes = maxBodyBytes;
    }
}
