/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.lucene;

import me.n1ar4.jar.analyzer.entity.LuceneSearchResult;
import me.n1ar4.jar.analyzer.gui.MainForm;

import javax.swing.*;
import java.awt.*;

public class LuceneResultRender extends DefaultListCellRenderer {
    @Override
    @SuppressWarnings("all")
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof LuceneSearchResult) {
            LuceneSearchResult result = (LuceneSearchResult) value;
            if (result.getType() == LuceneSearchResult.TYPE_CLASS_NAME) {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append("<font style=\"color: blue; font-weight: bold;\">");
                sb.append("type: ");
                sb.append("</font>");
                sb.append("<font style=\"color: purple; font-weight: bold;\">");
                sb.append("class/file");
                sb.append("</font>");
                sb.append("<font style=\"color: blue; font-weight: bold;\">");
                sb.append(" class name: ");
                sb.append("</font>");
                sb.append("<font style=\"color: orange; font-weight: bold;\">");
                sb.append(result.getClassName());
                sb.append("</font>");
                sb.append("</html>");
                setText(sb.toString());
            } else if (result.getType() == LuceneSearchResult.TYPE_CONTENT) {
                StringBuilder sb = new StringBuilder();
                sb.append("<html>");
                sb.append("<font style=\"color: blue; font-weight: bold;\">");
                sb.append("type: ");
                sb.append("</font>");
                sb.append("<font style=\"color: green; font-weight: bold;\">");
                sb.append("code/content");
                sb.append("</font>");
                sb.append("<font style=\"color: blue; font-weight: bold;\">");
                sb.append(" class name: ");
                sb.append("</font>");
                sb.append("<font style=\"color: orange; font-weight: bold;\">");
                sb.append(result.getClassName());
                sb.append("</font>");
                sb.append("</html>");
                setText(sb.toString());
            } else {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "错误的 Lucene 搜索结果");
                return null;
            }
        } else {
            return null;
        }
        return component;
    }
}
