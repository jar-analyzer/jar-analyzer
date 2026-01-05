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

import me.n1ar4.jar.analyzer.gui.GlobalOptions;
import me.n1ar4.jar.analyzer.gui.MainForm;

import java.awt.*;
import java.net.URI;

public class APIAction {
    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null) {
            return;
        }

        instance.getBindText().setText(GlobalOptions.getServerConfig().getBind());
        instance.getAuthText().setText(GlobalOptions.getServerConfig().isAuth() ? "TRUE" : "FALSE");
        instance.getPortText().setText(String.valueOf(GlobalOptions.getServerConfig().getPort()));
        String token = GlobalOptions.getServerConfig().getToken();
        if (token != null && !token.isEmpty()) {
            int length = token.length();
            if (length > 2) {
                char[] chars = token.toCharArray();
                for (int i = 1; i < length - 1; i++) {
                    chars[i] = '*';
                }
                token = new String(chars);
            } else {
                char[] masked = new char[length];
                java.util.Arrays.fill(masked, '*');
                token = new String(masked);
            }
        }
        instance.getTokenText().setText(token);

        instance.getApiDocBtn().addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(
                        "https://github.com/jar-analyzer/jar-analyzer/blob/master/doc/README-api.md"));
            } catch (Exception ignored) {
            }
        });

        instance.getMcpDocBtn().addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(
                        "https://github.com/jar-analyzer/jar-analyzer/blob/master/mcp-doc/README.md"));
            } catch (Exception ignored) {
            }
        });

        instance.getN8nDocBtn().addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(
                        "https://github.com/jar-analyzer/jar-analyzer/blob/master/n8n-doc/README.md"));
            } catch (Exception ignored) {
            }
        });
    }
}
