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
 * MCP 启动阶段
 * 用于在 GUI 中向用户展示启动进度
 */
public enum McpStartStage {
    /**
     * 校验配置
     */
    VALIDATE("校验配置参数"),
    /**
     * 检测端口占用
     */
    PORT_CHECK("检测端口是否被占用"),
    /**
     * 注册工具
     */
    REGISTER_TOOLS("注册 MCP 工具"),
    /**
     * 绑定监听
     */
    BIND("绑定监听端口"),
    /**
     * 启动 accept 线程
     */
    ACCEPT("启动 Accept 线程"),
    /**
     * 完成
     */
    DONE("启动完成");

    private final String description;

    McpStartStage(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int total() {
        return values().length;
    }

    public int order() {
        return ordinal() + 1;
    }
}
