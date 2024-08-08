package me.n1ar4.jar.analyzer.gui.util;

import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import java.util.List;

public class CodeMenuHelper {
    public static void run() {
        RSyntaxTextArea rArea = (RSyntaxTextArea) MainForm.getCodeArea();
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem selectItem = new JMenuItem("SELECT STRING (LDC)");
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
        rArea.setPopupMenu(popupMenu);
    }
}
