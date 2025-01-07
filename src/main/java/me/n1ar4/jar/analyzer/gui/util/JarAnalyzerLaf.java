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

import java.awt.*;
import java.util.Objects;

public class JarAnalyzerLaf extends FlatIntelliJLaf {
    public static boolean setup() {
        setupLight();
        return true;
    }

    public static void setupDark(){
        try {
            FlatLaf.setup(IntelliJTheme.createLaf(Objects.requireNonNull(
                    JarAnalyzerLaf.class.getClassLoader().getResourceAsStream("theme/dark.json"))));
            MainForm.getCodeArea().setBackground(new Color(229, 229, 255));
        } catch (Exception ignored) {
        }
        FlatLaf.updateUI();
    }

    public static void setupLight() {
        setup(new JarAnalyzerLaf());
        FlatLaf.updateUI();
    }
}
