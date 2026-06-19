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

import me.n1ar4.jar.analyzer.mcp.tools.JarAnalyzerTools;
import me.n1ar4.jar.analyzer.mcp.tools.ToolRegistry;
import me.n1ar4.jar.analyzer.utils.SocketUtil;

import java.io.IOException;

/**
 * MCP 启动器
 * 进程内单例：保证一次只起一个 MCP 服务器
 */
public class McpServerLauncher {

    private static McpServerLauncher instance;
    private static final Object LOCK = new Object();

    private McpServer server;
    private McpConfig config;

    private McpServerLauncher() {
    }

    public static McpServerLauncher getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new McpServerLauncher();
            }
            return instance;
        }
    }

    /**
     * 一次性注册全部内置工具（幂等）
     */
    public void initToolsIfNeeded() {
        if (ToolRegistry.getInstance().size() == 0) {
            JarAnalyzerTools.registerAll();
        }
    }

    public synchronized boolean isRunning() {
        return server != null && server.isRunning();
    }

    public synchronized McpConfig getConfig() {
        return config;
    }

    public synchronized McpServer getServer() {
        return server;
    }

    /**
     * 启动 MCP 服务器
     *
     * @throws IOException     启动失败（端口占用等）
     * @throws IllegalStateException 已经在运行
     */
    public synchronized void start(McpConfig cfg, McpEventListener listener) throws IOException {
        if (server != null && server.isRunning()) {
            throw new IllegalStateException("MCP server already running");
        }
        if (cfg == null) {
            throw new IllegalArgumentException("config is null");
        }
        // 端口占用检查
        if (SocketUtil.isPortInUse("127.0.0.1", cfg.getPort())) {
            throw new IOException("port " + cfg.getPort() + " is in use");
        }
        initToolsIfNeeded();
        this.config = cfg;
        this.server = new McpServer(cfg);
        this.server.setListener(listener);
        this.server.start();
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
