/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai;

import com.formdev.flatlaf.FlatIntelliJLaf;
import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.JarAnalyzerLaf;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * AI 弹窗强制浅色主题工具
 * <p>
 * 设计目标：
 * - AI 配置 / 对话窗口的视觉只在浅色背景下做了优化
 * - 用户主程序使用暗色 / 橙色主题时，AI 弹窗仍保持浅色
 * - 弹窗关闭后恢复用户原主题，保证主程序后续行为一致
 * <p>
 * 实现思路：
 * 1. 显示前：把全局 LaF 切到浅色 FlatIntelliJLaf
 * 2. 仅对 dialog 子树调 updateComponentTreeUI
 * 3. 主程序已存在窗口因为没被刷新，UI 实例保持原主题不变
 * 4. dialog 关闭后：根据用户已保存的主题，调用项目自身的 JarAnalyzerLaf.setupXxx
 * 重新还原全局 LaF 状态，保证后续新开窗口主题正确
 */
final class LightLafContext {
    private static final Logger logger = LogManager.getLogger();

    private LightLafContext() {
    }

    /**
     * 在 setVisible(true) 之前调用：把窗口子树渲染为浅色；
     * 当窗口关闭时自动恢复全局 LaF 到用户原主题。
     */
    static void applyLightTo(Window window) {
        if (window == null) {
            return;
        }
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
            SwingUtilities.updateComponentTreeUI(window);
        } catch (Throwable ex) {
            logger.error("apply light laf to dialog failed: {}", ex.toString());
            return;
        }
        // 注册关闭钩子：弹窗关闭后恢复原主题（仅切全局 LaF，不刷新已存在窗口）
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                restoreUserTheme();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                restoreUserTheme();
            }
        });
    }

    /**
     * 根据 MainForm 中的 ConfigFile 重新调用 JarAnalyzerLaf.setupXxx；
     * 这些方法内部会安全地 setLookAndFeel 并仅对必要资源（如代码区主题）做更新。
     */
    private static void restoreUserTheme() {
        try {
            String theme = "default";
            if (MainForm.getInstance() != null) {
                ConfigFile cf = MainForm.getConfig();
                if (cf != null && cf.getTheme() != null && !cf.getTheme().isEmpty()) {
                    theme = cf.getTheme();
                }
            }
            switch (theme) {
                case "dark":
                    JarAnalyzerLaf.setupDark();
                    break;
                case "orange":
                    JarAnalyzerLaf.setupOrange();
                    break;
                default:
                    // 浅色：当前 LaF 已经是浅色 FlatIntelliJLaf，不需要重新切换
                    // 直接 set 一个新实例避免遗留状态
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    break;
            }
        } catch (Throwable ex) {
            logger.error("restore user theme failed: {}", ex.toString());
        }
    }
}
