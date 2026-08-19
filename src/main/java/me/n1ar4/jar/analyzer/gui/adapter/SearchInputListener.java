/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.adapter;

import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.core.SqlSessionFactoryUtil;
import me.n1ar4.jar.analyzer.core.mapper.ClassMapper;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.tree.FileTree;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import org.apache.ibatis.session.SqlSession;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.List;

public class SearchInputListener implements DocumentListener {
    private static SqlSession searchSession;
    private static ClassMapper classMapper;

    private static ClassMapper mapper() {
        if (classMapper == null) {
            searchSession = SqlSessionFactoryUtil.sqlSessionFactory.openSession(true);
            classMapper = searchSession.getMapper(ClassMapper.class);
        }
        return classMapper;
    }

    /**
     * 数据库重建前调用：关闭这个常驻搜索会话，避免占用旧库文件。
     * 下次搜索时按需用重建后的新工厂重新打开
     */
    public static void resetSession() {
        try {
            if (searchSession != null) {
                searchSession.close();
            }
        } catch (Throwable ignored) {
        }
        searchSession = null;
        classMapper = null;
    }
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
            fileTreeSearchLabel.setToolTipText(temp[temp.length - 1]);
            return;
        }
        count = 0;
        refresh = true;
        if (!StrUtil.isNotBlank(string)) {
            fileTreeSearchTextField.setText("");
            return;
        }
        collect = mapper().includeClassByClassName(string);
        if (!collect.isEmpty()) {
            String className = collect.get(0);
            boolean innerClass = className.contains("$");
            String[] temp = className.split("/");
            fileTree.searchPathTarget(collect.get(0));
            fileTreeSearchLabel.setText(StrUtil.format("<html><p> result: {} / {} ({}) </p>" +
                            "<p> class: {} </p>" +
                            "</html>",
                    1, collect.size(), innerClass ? "inner class" : "normal", temp[temp.length - 1]));
            fileTreeSearchLabel.setToolTipText(temp[temp.length - 1]);
            fileTreeSearchLabel.setVisible(true);
        } else {
            fileTreeSearchLabel.setToolTipText(null);
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
