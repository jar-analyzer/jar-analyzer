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

import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;
import java.awt.*;

/**
 * 代码区域与界面字体大小设置对话框（两者分开指定）
 */
public class FontSizeDialog {
    /**
     * @return {代码区字号, 界面字号}，取消或输入非法时返回 null
     */
    public static float[] show() {
        JTextField codeField = new JTextField(String.valueOf((int) MainForm.FONT_SIZE), 5);
        JTextField uiField = new JTextField(String.valueOf((int) MainForm.UI_FONT_SIZE), 5);

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("代码区域字体大小 (10-50)："));
        panel.add(codeField);
        panel.add(new JLabel("界面字体大小 (10-50)："));
        panel.add(uiField);

        // 2025/06/14 修复有时候找不到 DIALOG 的问题
        JDialog topDialog = new JDialog();
        topDialog.setAlwaysOnTop(true);
        topDialog.setModal(true);
        topDialog.setLocationRelativeTo(null);
        int res = JOptionPane.showConfirmDialog(topDialog, panel,
                "设置字体大小", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, IconManager.ausIcon);
        topDialog.dispose();
        if (res != JOptionPane.OK_OPTION) {
            return null;
        }
        try {
            int code = Integer.parseInt(codeField.getText().trim());
            int ui = Integer.parseInt(uiField.getText().trim());
            if (code < 10 || code > 50 || ui < 10 || ui > 50) {
                JOptionPane.showMessageDialog(null, "字体大小必须在 10 到 50 之间",
                        "警告", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return new float[]{code, ui};
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "请输入有效的数字",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
