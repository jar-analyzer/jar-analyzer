/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.IntelliJTheme;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JarAnalyzerLaf extends FlatIntelliJLaf {
    private static final Logger logger = LogManager.getLogger();
    private static volatile String currentTheme = "light";

    public static boolean setup() {
        setupLight(true);
        return true;
    }

    /**
     * 设置全局界面字体（菜单/按钮/标签等）。FlatLaf 的 globalExtraDefaults
     * 只接受字符串值（字体语法 "family,style,size"），且只对之后创建的
     * LaF 实例生效，必须在 FlatLaf.setup 之前调用
     */
    public static void applyGlobalUiFont() {
        try {
            Font base = UIManager.getLookAndFeelDefaults().getFont("defaultFont");
            if (base == null) {
                base = UIManager.getFont("Label.font");
            }
            String family = base != null ? base.getFamily() : Font.DIALOG;
            Map<String, String> extras = new HashMap<>();
            extras.put("defaultFont", family + ",plain," + (int) MainForm.UI_FONT_SIZE);
            FlatLaf.setGlobalExtraDefaults(extras);
        } catch (Throwable t) {
            logger.warn("apply ui font failed: {}", t.toString());
        }
    }

    /**
     * 按当前主题重建 LaF（界面字体设置随之生效，代码区主题与字体同步刷新）
     */
    public static void refreshUiFont() {
        if ("dark".equals(currentTheme)) {
            setupDark();
        } else if ("orange".equals(currentTheme)) {
            setupOrange();
        } else {
            setupLight(false);
        }
    }

    public static void setupDark() {
        currentTheme = "dark";
        applyGlobalUiFont();
        try {
            ClassLoader classLoader = JarAnalyzerLaf.class.getClassLoader();
            FlatLaf.setup(IntelliJTheme.createLaf(
                    Objects.requireNonNull(classLoader.getResourceAsStream("theme/dark.json"))));
            Theme theme = Theme.load(
                    classLoader.getResourceAsStream("syntax/dark.xml"));
            theme.apply((RSyntaxTextArea) MainForm.getCodeArea());
            MainForm.getCodeArea().setFont(
                    MainForm.getCodeArea().getFont().deriveFont(MainForm.FONT_SIZE));
            // 同步更新所有 Tab 的语法主题
            CodeTabPanel tabPanel = MainForm.getCodeTabPanel();
            if (tabPanel != null) {
                tabPanel.applyThemeToAllTabs("syntax/dark.xml");
            }
        } catch (Exception ex) {
            logger.error("change theme failed: {}", ex);
        }
        FlatLaf.updateUI();
    }

    public static void setupOrange() {
        currentTheme = "orange";
        applyGlobalUiFont();
        try {
            ClassLoader classLoader = JarAnalyzerLaf.class.getClassLoader();
            FlatLaf.setup(IntelliJTheme.createLaf(
                    Objects.requireNonNull(classLoader.getResourceAsStream("theme/orange.json"))));
            Theme theme = Theme.load(
                    classLoader.getResourceAsStream("syntax/default.xml"));
            theme.apply((RSyntaxTextArea) MainForm.getCodeArea());
            MainForm.getCodeArea().setFont(
                    MainForm.getCodeArea().getFont().deriveFont(MainForm.FONT_SIZE));
            // 同步更新所有 Tab 的语法主题
            CodeTabPanel tabPanel = MainForm.getCodeTabPanel();
            if (tabPanel != null) {
                tabPanel.applyThemeToAllTabs("syntax/default.xml");
            }
        } catch (Exception ex) {
            logger.error("change theme failed: {}", ex);
        }
        FlatLaf.updateUI();
    }

    public static void setupLight(boolean init) {
        currentTheme = "light";
        applyGlobalUiFont();
        try {
            ClassLoader classLoader = JarAnalyzerLaf.class.getClassLoader();
            setup(new JarAnalyzerLaf());
            if (!init) {
                Theme theme = Theme.load(
                        classLoader.getResourceAsStream("syntax/default.xml"));
                theme.apply((RSyntaxTextArea) MainForm.getCodeArea());
                MainForm.getCodeArea().setFont(
                        MainForm.getCodeArea().getFont().deriveFont(MainForm.FONT_SIZE));
                // 同步更新所有 Tab 的语法主题
                CodeTabPanel tabPanel = MainForm.getCodeTabPanel();
                if (tabPanel != null) {
                    tabPanel.applyThemeToAllTabs("syntax/default.xml");
                }
            }
        } catch (Exception ex) {
            logger.error("change theme failed: {}", ex);
        }
        FlatLaf.updateUI();
    }
}
