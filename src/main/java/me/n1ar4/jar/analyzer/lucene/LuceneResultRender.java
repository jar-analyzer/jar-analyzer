/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
