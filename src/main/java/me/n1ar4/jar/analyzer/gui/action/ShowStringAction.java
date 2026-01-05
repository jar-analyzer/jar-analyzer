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

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.ShowStringForm;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;

import javax.swing.*;
import java.util.ArrayList;

public class ShowStringAction {
    public static void run() {
        JButton showString = MainForm.getInstance().getShowStringListButton();
        showString.addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "please start engine first");
                return;
            }

            // 2025/06/26 优化 ALL STRING 字符串展示和引导
            StringBuilder show = new StringBuilder();
            int allStringSize = MainForm.getEngine().getStringCount();
            if (allStringSize > 1000) {
                show.append("字符串数量过大不易展示，请前往 SEARCH 搜索面板进行精确的字符串搜索");
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        show.toString());
                MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);
                return;
            } else {
                show.append("该功能仅简单展示字符串，请前往 SEARCH 搜索面板进行精确的字符串搜索");
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        show.toString());
            }

            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            new Thread(() -> {
                ArrayList<String> stringList = MainForm.getEngine().getStrings(1);
                int total = MainForm.getEngine().getStringCount();
                ShowStringForm.start(total, stringList, dialog);
            }).start();
        });
    }
}
