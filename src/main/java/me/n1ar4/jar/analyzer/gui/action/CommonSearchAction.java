package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import javax.swing.*;

public class CommonSearchAction {
    public static void run() {
        JRadioButton methodCallR = MainForm.getInstance().getMethodCallRadioButton();
        JRadioButton methodDefR = MainForm.getInstance().getMethodDefinitionRadioButton();
        JRadioButton strContainsR = MainForm.getInstance().getStringContainsRadioButton();
        JRadioButton binaryR = MainForm.getInstance().getBinarySearchRadioButton();
        methodCallR.addActionListener(e -> {
            if (methodCallR.isSelected()) {
                LogUtil.info("select method call search");
                MainForm.getInstance().getSearchClassText().setEnabled(true);
                MainForm.getInstance().getSearchMethodText().setEnabled(true);
                MainForm.getInstance().getSearchStrText().setText(null);
                MainForm.getInstance().getSearchStrText().setEnabled(false);
            }
        });
        methodDefR.addActionListener(e -> {
            if (methodDefR.isSelected()) {
                LogUtil.info("select method def search");
                MainForm.getInstance().getSearchClassText().setEnabled(true);
                MainForm.getInstance().getSearchMethodText().setEnabled(true);
                MainForm.getInstance().getSearchStrText().setText(null);
                MainForm.getInstance().getSearchStrText().setEnabled(false);
            }
        });
        strContainsR.addActionListener(e -> {
            if (strContainsR.isSelected()) {
                LogUtil.info("select string contains search");
                MainForm.getInstance().getSearchClassText().setText(null);
                MainForm.getInstance().getSearchMethodText().setText(null);
                MainForm.getInstance().getSearchClassText().setEnabled(false);
                MainForm.getInstance().getSearchMethodText().setEnabled(false);
                MainForm.getInstance().getSearchStrText().setEnabled(true);
            }
        });
        binaryR.addActionListener(e -> {
            if (binaryR.isSelected()) {
                LogUtil.info("select binary search");
                MainForm.getInstance().getSearchClassText().setText(null);
                MainForm.getInstance().getSearchMethodText().setText(null);
                MainForm.getInstance().getSearchClassText().setEnabled(false);
                MainForm.getInstance().getSearchMethodText().setEnabled(false);
                MainForm.getInstance().getSearchStrText().setEnabled(true);
            }
        });
    }
}
