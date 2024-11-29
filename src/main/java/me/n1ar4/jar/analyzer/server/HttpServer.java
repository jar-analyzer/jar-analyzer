/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.server;

import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.SocketUtil;

import javax.swing.*;

public class HttpServer {
    public static void start() {
        if (SocketUtil.isPortInUse("localhost", GlobalOptions.getServerPort())) {
            JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                    "<html>" +
                            "<p>无法启动 API SERVER 因为端口 " + GlobalOptions.getServerPort() + "被占用</p>" +
                            "<p>请使用 java -jar jar-analyzer.jar gui --port [其他] 修改端口</p>" +
                            "</html>");
            return;
        }
        new JarAnalyzerServer();
    }
}
