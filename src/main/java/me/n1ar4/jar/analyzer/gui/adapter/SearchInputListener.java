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

package me.n1ar4.jar.analyzer.gui.adapter;

import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.core.SqlSessionFactoryUtil;
import me.n1ar4.jar.analyzer.core.mapper.ClassMapper;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.tree.FileTree;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.List;

public class SearchInputListener implements DocumentListener {
    @SuppressWarnings("all")
    private static final ClassMapper classMapper = SqlSessionFactoryUtil.sqlSessionFactory.openSession(
            true).getMapper(ClassMapper.class);
    private static final FileTree fileTree = MainForm.getInstance().getFileTree();
    private static final JTextField fileTreeSearchTextField = MainForm.getInstance().getFileTreeSearchTextField();
    private static final JLabel fileTreeSearchLabel = MainForm.getInstance().getFileTreeSearchLabel();
    private static List<String> collect;
    private static int count = 0;
    private static boolean refresh = false;

    public static FileTree getFileTree() {
        return fileTree;
    }

    public static void search(String string, boolean isInner) {
        if (!isInner) {
            if (collect.isEmpty()) {
                return;
            }
            if (count == collect.size()) {
                count = 0;
            }
            if (count != 0 && refresh) {
                count++;
            }
            String className = collect.get(count++);
            boolean innerClass = className.contains("$");
            String[] temp = className.split("/");
            fileTree.searchPathTarget(className);
            refresh = false;
            fileTreeSearchLabel.setText(StrUtil.format("<html><p> result: {} / {} ({}) </p>" +
                            "<p> class: {} </p>" +
                            "</html>",
                    count, collect.size(), innerClass ? "inner class" : "normal", temp[temp.length - 1]));
            return;
        }
        count = 0;
        refresh = true;
        if (!StrUtil.isNotBlank(string)) {
            fileTreeSearchTextField.setText("");
            return;
        }
        collect = classMapper.includeClassByClassName(string);
        if (!collect.isEmpty()) {
            String className = collect.get(0);
            boolean innerClass = className.contains("$");
            String[] temp = className.split("/");
            fileTree.searchPathTarget(collect.get(0));
            fileTreeSearchLabel.setText(StrUtil.format("<html><p> result: {} / {} ({}) </p>" +
                            "<p> class: {} </p>" +
                            "</html>",
                    1, collect.size(), innerClass ? "inner class" : "normal", temp[temp.length - 1]));
            fileTreeSearchLabel.setVisible(true);
        } else {
            fileTreeSearchLabel.setVisible(false);
        }
    }

    private void filterInput() {
        String text = fileTreeSearchTextField.getText();
        // 处理输入是中文的问题
        if (text.contains("'") || text.contains("\"")) {
            LogUtil.warn("check your input (invalid chars)");
            SwingUtilities.invokeLater(new Thread(() -> fileTreeSearchTextField.setText("")));
            return;
        }
        if (text.trim().isEmpty()) {
            return;
        }
        search(text, true);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        filterInput();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        filterInput();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        filterInput();
    }
}
