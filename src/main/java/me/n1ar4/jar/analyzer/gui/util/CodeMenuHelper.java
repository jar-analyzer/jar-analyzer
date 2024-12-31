/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.LuceneSearchForm;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.OpenUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.util.List;

public class CodeMenuHelper {
    final static JPanel fileTreeSearchPanel = MainForm.getInstance().getFileTreeSearchPanel();
    final static JTextField fileTreeSearchTextField = MainForm.getInstance().getFileTreeSearchTextField();

    public static void run() {
        RSyntaxTextArea rArea = (RSyntaxTextArea) MainForm.getCodeArea();
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem selectItem = new JMenuItem("SELECT STRING (LDC)");
        selectItem.setIcon(IconManager.stringIcon);
        selectItem.addActionListener(e -> {
            String str = rArea.getSelectedText();

            if (str == null) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "SELECTED STRING IS NULL");
                return;
            }

            new Thread(() -> {
                List<MethodResult> mrs = MainForm.getEngine().getMethodsByStr(str);
                DefaultListModel<MethodResult> searchData = new DefaultListModel<>();
                searchData.clear();
                for (MethodResult mr : mrs) {
                    searchData.addElement(mr);
                }
                MainForm.getInstance().getSearchList().setModel(searchData);
                MainForm.getInstance().getTabbedPanel().setSelectedIndex(1);
            }).start();
        });
        popupMenu.add(selectItem);

        JMenuItem searchCallItem = new JMenuItem("SEARCH CALL INFO");
        searchCallItem.setIcon(IconManager.callIcon);
        searchCallItem.addActionListener(e -> {
            String methodName = rArea.getSelectedText();

            if (methodName == null) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "SELECTED STRING IS NULL");
                return;
            }

            methodName = methodName.trim();

            String className = MainForm.getCurClass();

            String finalMethodName = methodName;
            new Thread(() -> {
                List<MethodResult> rL = MainForm.getEngine().getCallers(className, finalMethodName, null);
                List<MethodResult> eL = MainForm.getEngine().getCallee(className, finalMethodName, null);

                DefaultListModel<MethodResult> calleeData = (DefaultListModel<MethodResult>)
                        MainForm.getInstance().getCalleeList().getModel();
                DefaultListModel<MethodResult> callerData = (DefaultListModel<MethodResult>)
                        MainForm.getInstance().getCallerList().getModel();

                calleeData.clear();
                callerData.clear();

                for (MethodResult mr : rL) {
                    callerData.addElement(mr);
                }
                for (MethodResult mr : eL) {
                    calleeData.addElement(mr);
                }

                MainForm.getInstance().getTabbedPanel().setSelectedIndex(2);
            }).start();
        });
        popupMenu.add(searchCallItem);

        JMenuItem classItem = new JMenuItem("SEARCH CLASS FROM JARS");
        classItem.setIcon(IconManager.pubIcon);
        classItem.addActionListener(e -> {
            String className = rArea.getSelectedText();

            if (className == null) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "SELECTED STRING IS NULL");
                return;
            }

            className = className.trim();

            if (className.isEmpty()) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "SELECTED STRING IS NULL");
                return;
            }

            fileTreeSearchPanel.setVisible(true);

            fileTreeSearchTextField.setText(className);
        });
        popupMenu.add(classItem);

        JMenuItem openItem = new JMenuItem("OPEN IN EXPLORER");
        openItem.setIcon(IconManager.fileIcon);
        openItem.addActionListener(e -> OpenUtil.openCurrent());
        popupMenu.add(openItem);

        JMenuItem luceneItem = new JMenuItem("OPEN GLOBAL SEARCH");
        luceneItem.setIcon(IconManager.luceneIcon);
        luceneItem.addActionListener(e -> LuceneSearchForm.start(1));
        popupMenu.add(luceneItem);

        rArea.setPopupMenu(popupMenu);
    }
}
