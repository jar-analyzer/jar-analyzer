package me.n1ar4.jar.analyzer.gui.adapter;

import cn.hutool.core.util.StrUtil;
import me.n1ar4.jar.analyzer.core.SqlSessionFactoryUtil;
import me.n1ar4.jar.analyzer.core.mapper.ClassMapper;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.tree.FileTree;

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

    public static void search(String string, boolean isInner) {
        if (!isInner) {
            if (collect.isEmpty()) {
                return;
            }
            if (count == collect.size()) {
                count = 0;
            }
            if (refresh) {
                count++;
            }
            fileTree.searchPathTarget(collect.get(count++));
            refresh = false;
            fileTreeSearchLabel.setText(StrUtil.format("Find: {} Current:{}",
                    String.valueOf(collect.size()), count));
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
            fileTreeSearchLabel.setText(StrUtil.format("Find: {} Current:{}",
                    String.valueOf(collect.size()), 1));
            fileTreeSearchLabel.setVisible(true);
            fileTree.searchPathTarget(collect.get(0));
        } else {
            fileTreeSearchLabel.setVisible(false);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        search(fileTreeSearchTextField.getText(), true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        search(fileTreeSearchTextField.getText(), true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        search(fileTreeSearchTextField.getText(), true);
    }
}
