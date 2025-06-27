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

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import javax.swing.*;

public class CommonSearchAction {
    private static final String TIPS = "# 类名非必须 可简单类名 可全类名 可以不输入";

    public static void run() {
        JRadioButton methodCallR = MainForm.getInstance().getMethodCallRadioButton();
        JRadioButton methodDefR = MainForm.getInstance().getMethodDefinitionRadioButton();
        JRadioButton strContainsR = MainForm.getInstance().getStringContainsRadioButton();
        JRadioButton binaryR = MainForm.getInstance().getBinarySearchRadioButton();
        methodCallR.addActionListener(e -> {
            if (methodCallR.isSelected()) {
                LogUtil.info("select method call search");
                MainForm.getInstance().getEqualsSearchRadioButton().setEnabled(true);
                MainForm.getInstance().getLikeSearchRadioButton().setEnabled(true);
                MainForm.getInstance().getSearchClassText().setEnabled(true);
                // 2025/06/27 给出提示防止误导
                if (MainForm.getInstance().getSearchClassText().getText().isEmpty()) {
                    MainForm.getInstance().getSearchClassText().setText(TIPS);
                }
                MainForm.getInstance().getSearchMethodText().setEnabled(true);
                MainForm.getInstance().getSearchStrText().setText(null);
                MainForm.getInstance().getSearchStrText().setEnabled(false);
            }
        });
        methodDefR.addActionListener(e -> {
            if (methodDefR.isSelected()) {
                LogUtil.info("select method def search");
                MainForm.getInstance().getEqualsSearchRadioButton().setEnabled(true);
                MainForm.getInstance().getLikeSearchRadioButton().setEnabled(true);
                MainForm.getInstance().getSearchClassText().setEnabled(true);
                // 2025/06/27 给出提示防止误导
                if (MainForm.getInstance().getSearchClassText().getText().isEmpty()) {
                    MainForm.getInstance().getSearchClassText().setText(TIPS);
                }
                MainForm.getInstance().getSearchMethodText().setEnabled(true);
                MainForm.getInstance().getSearchStrText().setText(null);
                MainForm.getInstance().getSearchStrText().setEnabled(false);
            }
        });
        strContainsR.addActionListener(e -> {
            if (strContainsR.isSelected()) {
                LogUtil.info("select string contains search");
                // 2025/06/27 STRING 搜索应该支持 LIKE 和 精确匹配两种
                MainForm.getInstance().getEqualsSearchRadioButton().setEnabled(true);
                MainForm.getInstance().getLikeSearchRadioButton().setEnabled(true);

                if (MainForm.getInstance().getSearchClassText().getText().isEmpty()) {
                    MainForm.getInstance().getSearchClassText().setText(TIPS);
                }
                MainForm.getInstance().getSearchMethodText().setText(null);

                // 2025/04/08 FIX
                // 允许字符串搜索指定 CLASS
                // MainForm.getInstance().getSearchClassText().setEnabled(false);
                MainForm.getInstance().getSearchClassText().setEnabled(true);
                MainForm.getInstance().getSearchMethodText().setEnabled(false);
                MainForm.getInstance().getSearchStrText().setEnabled(true);
            }
        });
        binaryR.addActionListener(e -> {
            if (binaryR.isSelected()) {
                LogUtil.info("select binary search");
                MainForm.getInstance().getEqualsSearchRadioButton().setEnabled(false);
                MainForm.getInstance().getLikeSearchRadioButton().setEnabled(false);
                MainForm.getInstance().getSearchClassText().setText(null);
                MainForm.getInstance().getSearchMethodText().setText(null);
                MainForm.getInstance().getSearchClassText().setEnabled(false);
                MainForm.getInstance().getSearchMethodText().setEnabled(false);
                MainForm.getInstance().getSearchStrText().setEnabled(true);
            }
        });
    }
}
