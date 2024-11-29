/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OpenUtil {
    public static void open(String absPath) {
        if (OSUtil.isWindows()) {
            String cmd = String.format("start %s", absPath);
            String[] xrayCmd = new String[]{"cmd.exe", "/c", String.format("%s", cmd)};
            exec(xrayCmd);
        } else {
            String cmd = String.format("open %s", absPath);
            String[] xrayCmd = new String[]{"/bin/bash", "-c", String.format("%s", cmd)};
            exec(xrayCmd);
        }
    }

    private static void exec(String[] cmdArray) {
        try {
            String cmd = String.join(" ", cmdArray);
            LogUtil.info(String.format("run cmd: %s", cmd));
            new ProcessBuilder(cmdArray).start();
        } catch (Exception ignored) {
        }
    }

    public static void openCurrent() {
        String className = MainForm.getCurClass();
        if (className == null || className.isEmpty()) {
            MethodResult cur = MainForm.getCurMethod();
            if (cur != null) {
                className = cur.getClassName();
                if (className == null || className.isEmpty()) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "找不到当前类 无法打开");
                    return;
                }
                openClass(className);
            }
        } else {
            openClass(className);
        }
    }

    public static void openClass(String className) {
        String tempPath = className.replace("/", File.separator);
        String classPath;
        classPath = String.format("%s%s%s.class", Const.tempDir, File.separator, tempPath);
        if (!Files.exists(Paths.get(classPath))) {
            classPath = String.format("%s%sBOOT-INF%sclasses%s%s.class",
                    Const.tempDir, File.separator, File.separator, File.separator, tempPath);
            if (!Files.exists(Paths.get(classPath))) {
                classPath = String.format("%s%sWEB-INF%sclasses%s%s.class",
                        Const.tempDir, File.separator, File.separator, File.separator, tempPath);
                if (!Files.exists(Paths.get(classPath))) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "FILE NOT FOUND");
                    return;
                }
            }
        }
        String finalClassPath = classPath;
        OpenUtil.openFileInExplorer(Paths.get(finalClassPath).toAbsolutePath().toString());
    }

    public static void openFileInExplorer(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "FILE NOT FOUND: " + filePath);
            return;
        }

        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                Runtime.getRuntime().exec("explorer.exe /select," + file.getAbsolutePath());
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{
                        "open", "-R", file.getAbsolutePath()
                });
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(new String[]{
                        "xdg-open", file.getAbsolutePath()
                });
            } else {
                JOptionPane.showMessageDialog(null, "UNSUPPORTED OS: " + os);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "CANNOT SELECTED FILE: " + e.getMessage());
        }
    }
}
