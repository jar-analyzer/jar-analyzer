package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.ShowStringForm;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;

import javax.swing.*;
import java.util.ArrayList;

public class ShowStringAction {
    public static void run() {
        JButton showString = MainForm.getInstance().getShowStringListButton();
        showString.addActionListener(e -> {
            if (MainForm.getEngine() == null || !MainForm.getEngine().isEnabled()) {
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "please start engine first");
                return;
            }
            JDialog dialog = ProcessDialog.createProgressDialog(MainForm.getInstance().getMasterPanel());
            new Thread(() -> dialog.setVisible(true)).start();
            new Thread(() -> {
                ArrayList<String> stringList = MainForm.getEngine().getStrings(1);
                int total = MainForm.getEngine().getStringCount();
                ShowStringForm.start(total, stringList, dialog);
            }).start();
        });
    }
}
