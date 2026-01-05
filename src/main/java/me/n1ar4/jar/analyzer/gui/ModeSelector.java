/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui;

import me.n1ar4.jar.analyzer.gui.util.IconManager;
import me.n1ar4.jar.analyzer.starter.Const;

import javax.swing.*;
import java.awt.*;

public class ModeSelector {
    /**
     * 选择模式
     *
     * @return 0->取消 1->标准 2->快速
     */
    public static int show() {
        JLabel tipLabel = new JLabel("<html><body style='width: 320px'>" +
                "请选择分析模式：<br><br>" +
                "- 标准模式：记录方法调用关系、继承信息、WEB入口分析、字符串信息等<br>" +
                "- 快速模式：仅分析方法调用关系，不保存其他内容<br><br>" +
                "<p>一般情况下建议标准模式，可以生成完善的数据库</p>" +
                "</body></html>");

        JRadioButton standardMode = new JRadioButton("标准模式", true);
        JRadioButton quickMode = new JRadioButton("快速模式");

        ButtonGroup group = new ButtonGroup();
        group.add(standardMode);
        group.add(quickMode);

        JPanel panel = new JPanel(new BorderLayout(0, 10));

        panel.add(tipLabel, BorderLayout.NORTH);

        JPanel radioPanel = new JPanel(new GridLayout(1, 2, 0, 5));
        radioPanel.add(standardMode);
        radioPanel.add(quickMode);

        panel.add(radioPanel, BorderLayout.CENTER);

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                Const.ModeForm,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                IconManager.auIcon
        );

        if (result == JOptionPane.OK_OPTION) {
            if (standardMode.isSelected()) {
                return 1;
            }
            if (quickMode.isSelected()) {
                return 2;
            }
        }
        return 0;
    }
}