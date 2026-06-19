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
 * MCP 监听日志事件
 * 用于面板展示
 */
public interface McpEventListener {
    /**
     * 普通日志输出（INFO 级别）
     */
    void onLog(String message);

    /**
     * 警告
     */
    void onWarn(String message);

    /**
     * 错误
     */
    void onError(String message);

    /**
     * 一次完整的 MCP 请求统计
     *
     * @param transport 传输方式 (sse / streamable)
     * @param method    MCP 方法 (e.g. tools/call)
     * @param ok        是否成功
     */
    void onRequest(String transport, String method, boolean ok);

    /**
     * 客户端连接计数变化
     *
     * @param sse        当前 SSE 长连接数
     * @param streamable 当前 streamable 流响应数
     */
    void onConnectionChanged(int sse, int streamable);
}
