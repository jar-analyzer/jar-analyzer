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

    private static final ClassMapper classMapper = SqlSessionFactoryUtil.sqlSessionFactory.openSession(true).getMapper(ClassMapper.class);
    private static final FileTree fileTree = MainForm.getInstance().getFileTree();
    private static final JTextField fileTreeSearchtextField = MainForm.getInstance().getFileTreeSearchtextField();
    private static final JLabel fileTreeSearchlabel = MainForm.getInstance().getFileTreeSearchlabel();
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
            fileTreeSearchlabel.setText(StrUtil.format("Find: {} Current:{}", String.valueOf(collect.size()), count));
            return;
        }
        count = 0;
        refresh = true;
        if (!StrUtil.isNotBlank(string)) {
            fileTreeSearchtextField.setText("");
            return;
        }
        collect = classMapper.includeClassByClassName(string);
        if (!collect.isEmpty()) {
            fileTreeSearchlabel.setText(StrUtil.format("Find: {} Current:{}", String.valueOf(collect.size()), 1));
            fileTreeSearchlabel.setVisible(true);
            fileTree.searchPathTarget(collect.get(0));
        } else {
            fileTreeSearchlabel.setVisible(false);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        search(fileTreeSearchtextField.getText(), true);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        search(fileTreeSearchtextField.getText(), true);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        search(fileTreeSearchtextField.getText(), true);
    }
}
