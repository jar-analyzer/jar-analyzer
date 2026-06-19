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
}
