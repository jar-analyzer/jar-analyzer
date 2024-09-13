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

package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.entity.MethodResult;
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
        selectItem.setIcon(IconManager.javaIcon);
        popupMenu.add(selectItem);

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

        JMenuItem searchCallItem = new JMenuItem("SEARCH CALL INFO");
        searchCallItem.setIcon(IconManager.javaIcon);
        popupMenu.add(searchCallItem);

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

        JMenuItem classItem = new JMenuItem("SEARCH CLASS FROM JARS");
        classItem.setIcon(IconManager.javaIcon);
        popupMenu.add(classItem);

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

        JMenuItem openItem = new JMenuItem("OPEN IN EXPLORER");
        openItem.setIcon(IconManager.javaIcon);
        popupMenu.add(openItem);

        openItem.addActionListener(e -> OpenUtil.openCurrent());
        rArea.setPopupMenu(popupMenu);
    }
}
