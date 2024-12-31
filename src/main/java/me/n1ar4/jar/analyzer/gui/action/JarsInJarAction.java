/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.core.AnalyzeEnv;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import javax.swing.*;

public class JarsInJarAction {
    public static void run() {
        JCheckBox jarsInJar = MainForm.getInstance().getResolveJarsInJarCheckBox();
        jarsInJar.addActionListener(e -> {
            if (!jarsInJar.isSelected()) {
                LogUtil.info("not use jars in jar");
                AnalyzeEnv.jarsInJar = false;
            } else {
                LogUtil.info("use jars in jar");
                AnalyzeEnv.jarsInJar = true;
            }
        });
    }
}
