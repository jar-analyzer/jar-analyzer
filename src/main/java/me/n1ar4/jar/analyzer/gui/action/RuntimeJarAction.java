package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RuntimeJarAction {
    private static final String NOT_FOUND = "file not found";

    public static void run() {
        JCheckBox findRtBox = MainForm.getInstance().getAutoFindRtJarCheckBox();
        JCheckBox addRtBox = MainForm.getInstance().getAddRtJarWhenCheckBox();
        JTextField rtText = MainForm.getInstance().getRtText();

        findRtBox.addActionListener(e -> {
            if (findRtBox.isSelected()) {
                LogUtil.info("start find rt.jar file");
                String javaHome = System.getProperty("java.home");
                String rtJarPath = javaHome + File.separator + "lib" + File.separator + "rt.jar";
                if (Files.exists(Paths.get(rtJarPath))) {
                    LogUtil.info("rt.jar file found");
                    rtText.setText(rtJarPath);
                } else {
                    LogUtil.warn("rt.jar file not found");
                    rtText.setText(NOT_FOUND);
                }
            } else {
                LogUtil.info("clean rt.jar file path");
                rtText.setText(null);
            }
        });

        addRtBox.addActionListener(e -> {
            if (addRtBox.isSelected()) {
                if (StringUtil.isNull(rtText.getText())) {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "you must find rt.jar first");
                    addRtBox.setSelected(false);
                    return;
                }
                String rtJarPath = rtText.getText();
                if (Files.exists(Paths.get(rtJarPath))) {
                    LogUtil.info("add rt.jar");
                } else {
                    JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                            "file not found");
                    addRtBox.setSelected(false);
                }
            } else {
                LogUtil.info("not add rt.jar");
            }
        });
    }
}
