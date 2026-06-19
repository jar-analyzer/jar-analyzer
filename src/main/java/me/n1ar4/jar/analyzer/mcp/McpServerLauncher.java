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
     * 启动 MCP 服务器（兼容旧调用，无进度回调）
     */
    public synchronized void start(McpConfig cfg, McpEventListener listener) throws IOException {
        start(cfg, listener, null);
    }

    /**
     * 启动 MCP 服务器
     *
     * <p>步骤拆分以便上层 GUI 展示进度：</p>
     * <ol>
     *   <li>VALIDATE  - 校验入参</li>
     *   <li>PORT_CHECK - 检测端口占用（最耗时阶段之一）</li>
     *   <li>REGISTER_TOOLS - 注册工具</li>
     *   <li>BIND - 创建 ServerSocket + bind</li>
     *   <li>ACCEPT - 启动 Accept 线程</li>
     *   <li>DONE - 启动完成</li>
     * </ol>
     *
     * @param progress 阶段进度回调（可为 null）
     * @throws IOException           启动失败（端口占用等）
     * @throws IllegalStateException 已经在运行
     */
    public synchronized void start(McpConfig cfg,
                                   McpEventListener listener,
                                   McpStartProgress progress) throws IOException {
        notify(progress, McpStartStage.VALIDATE);
        if (server != null && server.isRunning()) {
            throw new IllegalStateException("MCP server already running");
        }
        if (cfg == null) {
            throw new IllegalArgumentException("config is null");
        }
        // 端口占用检查（这一步是阻塞的，可能要等 connect-timeout）
        notify(progress, McpStartStage.PORT_CHECK);
        if (SocketUtil.isPortInUse("127.0.0.1", cfg.getPort())) {
            throw new IOException("port " + cfg.getPort() + " is in use");
        }

        notify(progress, McpStartStage.REGISTER_TOOLS);
        initToolsIfNeeded();

        notify(progress, McpStartStage.BIND);
        this.config = cfg;
        this.server = new McpServer(cfg);
        this.server.setListener(listener);

        notify(progress, McpStartStage.ACCEPT);
        this.server.start();

        notify(progress, McpStartStage.DONE);
    }

    private static void notify(McpStartProgress p, McpStartStage stage) {
        if (p == null) return;
        try {
            p.onStage(stage);
        } catch (Throwable ignored) {
        }
        // 给 UI 一个最小的可见时长，让用户感知到阶段过渡
        // 注意：不在端口检测/绑定等"自然耗时"阶段后追加延时
        if (stage == McpStartStage.VALIDATE
                || stage == McpStartStage.REGISTER_TOOLS
                || stage == McpStartStage.ACCEPT) {
            try {
                Thread.sleep(120);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    /**
     * 阶段进度回调
     */
    public interface McpStartProgress {
        /**
         * 阶段进入回调
         * 在调用线程同步执行，请确保实现非阻塞
         */
        void onStage(McpStartStage stage);
    }
}
