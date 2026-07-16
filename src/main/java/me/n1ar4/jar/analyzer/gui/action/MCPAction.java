/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.gui.MCPPanel;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.SvgManager;
import me.n1ar4.jar.analyzer.mcp.McpServerLauncher;

import javax.swing.*;

/**
 * MCP 面板注册器
 *
 * <p>不侵入 MainForm 的布局代码，而是在主界面初始化结束后，
 * 通过 tabbedPanel.addTab(...) 把 MCPPanel 加进去。</p>
 *
 * <p>这样的好处：</p>
 * <ul>
 *  <li>MCP 面板与主窗口布局解耦，便于独立维护</li>
 *  <li>便于卸载/单测，控件都集中在 MCPPanel 内</li>
 * </ul>
 */
public class MCPAction {
    private static MCPPanel panelRef;

    public static void register() {
        MainForm instance = MainForm.getInstance();
        if (instance == null || instance.getTabbedPanel() == null) {
            return;
        }
        // 提前注册工具，让面板首次出现时即看到
        McpServerLauncher.getInstance().initToolsIfNeeded();

        JTabbedPane tabbed = instance.getTabbedPanel();
        // 防止重复添加（在某些重新初始化场景）
        for (int i = 0; i < tabbed.getTabCount(); i++) {
            if ("MCP".equals(tabbed.getTitleAt(i))
                    || "Mcp Server".equalsIgnoreCase(tabbed.getTitleAt(i))) {
                return;
            }
        }
        panelRef = new MCPPanel();
        tabbed.addTab("MCP", panelRef);
        int idx = tabbed.getTabCount() - 1;
        try {
            tabbed.setIconAt(idx, SvgManager.ConnectIcon);
        } catch (Throwable ignored) {
        }
    }

    public static MCPPanel getPanel() {
        return panelRef;
    }
}
