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
        JMenuItem searchCallItem = new JMenuItem("SEARCH CALL INFO");
        popupMenu.add(searchCallItem);
        searchCallItem.addActionListener(e -> {
            String methodName = rArea.getSelectedText();

            if (methodName == null) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "SELECTED STRING IS NULL");
                return;
            }

            String className = MainForm.getCurClass();

            List<MethodResult> rL = MainForm.getEngine().getCallers(className, methodName, null);
            List<MethodResult> eL = MainForm.getEngine().getCallee(className, methodName, null);

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
        });
        rArea.setPopupMenu(popupMenu);
    }
}
