/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
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

import java.util.Objects;

public class JarAnalyzerLaf extends FlatIntelliJLaf {
    private static final Logger logger = LogManager.getLogger();

    public static boolean setup() {
        setupLight(true);
        return true;
    }

    public static void setupDark() {
        try {
            ClassLoader classLoader = JarAnalyzerLaf.class.getClassLoader();
            FlatLaf.setup(IntelliJTheme.createLaf(
                    Objects.requireNonNull(classLoader.getResourceAsStream("theme/dark.json"))));
            Theme theme = Theme.load(
                    classLoader.getResourceAsStream("syntax/dark.xml"));
            theme.apply((RSyntaxTextArea) MainForm.getCodeArea());
        } catch (Exception ex) {
            logger.error("change theme failed: {}", ex);
        }
        FlatLaf.updateUI();
    }

    public static void setupLight(boolean init) {
        try {
            ClassLoader classLoader = JarAnalyzerLaf.class.getClassLoader();
            setup(new JarAnalyzerLaf());
            if (!init) {
                Theme theme = Theme.load(
                        classLoader.getResourceAsStream("syntax/default.xml"));
                theme.apply((RSyntaxTextArea) MainForm.getCodeArea());
            }
        } catch (Exception ex) {
            logger.error("change theme failed: {}", ex);
        }
        FlatLaf.updateUI();
    }
}
