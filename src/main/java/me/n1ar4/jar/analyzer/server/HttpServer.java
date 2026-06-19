/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.server;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.SocketUtil;

import javax.swing.*;

/**
 * 启动 jar-analyzer 内置 HTTP API Server
 *
 * <p>已固化为：仅本机 127.0.0.1:10032 监听，无任何鉴权。</p>
 */
public class HttpServer {
    public static void start(ServerConfig config) {
        if (SocketUtil.isPortInUse(ServerConfig.BIND, ServerConfig.PORT)) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "<html>" +
                            "<p>无法启动 API SERVER，因为端口 " + ServerConfig.PORT + " 被占用</p>" +
                            "<p>请关闭占用该端口的进程后再启动 jar-analyzer</p>" +
                            "</html>");
            return;
        }
        new JarAnalyzerServer(config);
    }
}
