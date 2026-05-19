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
    /**
     * 超过此阈值仅做友好提示，不再阻断（新版用 JList 渲染，不会卡顿）。
     */
    private static final int LARGE_STRING_HINT = 5000;

    public static void run() {
        JButton showString = MainForm.getInstance().getShowStringListButton();
        showString.addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "please start engine first");
                return;
            }

            int allStringSize = MainForm.getEngine().getStringCount();

            // 给一个温和的提示：数量很大时建议用 SEARCH 精确搜索；
            // 但不再像旧版那样强制 return —— JList 渲染对几千上万条都没问题。
            if (allStringSize > LARGE_STRING_HINT) {
                int resp = JOptionPane.showConfirmDialog(
                        MainForm.getInstance().getMasterPanel(),
                        "<html>" +
                                "<p>当前共有 " + allStringSize + " 条字符串，数量较多。</p>" +
                                "<p>建议优先使用 SEARCH 面板按关键词精确搜索。</p>" +
                                "<p>仍要打开 All Strings 列表？</p>" +
                                "</html>",
                        "提示",
                        JOptionPane.OK_CANCEL_OPTION);
                if (resp != JOptionPane.OK_OPTION) {
                    MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);
                    return;
                }
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
