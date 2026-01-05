/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.jar.analyzer.utils.DirUtil;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ExportForm {
    private JPanel masterPanel;
    private JTextField outputDirText;
    private JLabel outputDirLabel;
    private JRadioButton fernRadio;
    private JLabel engineLabel;
    private JTextArea jarsText;
    private JButton startBtn;
    private JLabel actionLabel;
    private JLabel jarLabel;
    private JLabel noteLabel;
    private JLabel noteValLabel;
    private JScrollPane jarsScroll;

    private static volatile boolean isRunning = false;

    public ExportForm() {
        fernRadio.setEnabled(false);
        fernRadio.setSelected(true);
        outputDirText.setText("jar-analyzer-export");

        // 初始参数
        try {
            ArrayList<String> path = MainForm.getEngine().getJarsPath();
            StringBuilder sb = new StringBuilder();
            for (String s : path) {
                sb.append(s);
                sb.append("\n");
            }
            String s = sb.toString();
            if (!s.trim().isEmpty()) {
                jarsText.setText(s.substring(0, s.length() - 1).trim());
            }
        } catch (Exception ignored) {
            jarsText.setText(null);
        }

        startBtn.addActionListener(e -> {
            if (outputDirText.getText().isEmpty()) {
                JOptionPane.showMessageDialog(masterPanel, "please enter the output directory");
                return;
            }
            if (isRunning) {
                JOptionPane.showMessageDialog(masterPanel, "decompile is running...");
                return;
            }

            if (jarsText.getText() == null || jarsText.getText().isEmpty()) {
                JOptionPane.showMessageDialog(masterPanel, "need jar input");
                return;
            }

            ArrayList<String> decompileJars = new ArrayList<>();
            String input = jarsText.getText().trim();
            // 多个 JAR 文件
            if (input.contains("\n")) {
                String[] items = input.split("\n");
                for (String item : items) {
                    if (!item.toLowerCase().endsWith(".jar")) {
                        continue;
                    }
                    Path itemPath = Paths.get(item);
                    if (Files.exists(itemPath)) {
                        decompileJars.add(itemPath.toAbsolutePath().toString());
                    }
                }
            } else {
                Path itemPath = Paths.get(input);
                if (Files.isDirectory(itemPath)) {
                    // 是 JAR 目录
                    if (Files.exists(itemPath)) {
                        // 添加所有 JAR 到里面
                        List<String> files = DirUtil.GetFiles(itemPath.toAbsolutePath().toString());
                        for (String file : files) {
                            if (!file.toLowerCase().endsWith(".jar")) {
                                continue;
                            }
                            Path filePath = Paths.get(file);
                            if (Files.exists(filePath)) {
                                decompileJars.add(filePath.toAbsolutePath().toString());
                            }
                        }
                    }
                } else {
                    // 是一个 JAR
                    if (input.toLowerCase().endsWith(".jar")) {
                        decompileJars.add(input);
                    }
                }
            }

            JDialog dialog = ProcessDialog.createProgressDialog(this.masterPanel);

            new Thread(() -> dialog.setVisible(true)).start();

            new Thread(() -> {
                isRunning = true;
                boolean success = DecompileEngine.decompileJars(decompileJars, outputDirText.getText());
                if (success) {
                    dialog.dispose();
                    JOptionPane.showMessageDialog(masterPanel, "jars decompiled successfully");
                    isRunning = false;
                }
            }).start();
        });
    }

    public static void start() {
        JFrame frame = new JFrame(Const.ExportForm);
        frame.setContentPane(new ExportForm().masterPanel);
        frame.pack();
        frame.setAlwaysOnTop(false);
        frame.setLocationRelativeTo(MainForm.getInstance().getMasterPanel());
        frame.setVisible(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        masterPanel = new JPanel();
        masterPanel.setLayout(new GridLayoutManager(6, 2, new Insets(5, 5, 5, 5), -1, -1));
        outputDirLabel = new JLabel();
        outputDirLabel.setText("OUTPUT DIR");
        masterPanel.add(outputDirLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final Spacer spacer1 = new Spacer();
        masterPanel.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        outputDirText = new JTextField();
        masterPanel.add(outputDirText, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        engineLabel = new JLabel();
        engineLabel.setText("ENGINE");
        masterPanel.add(engineLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        fernRadio = new JRadioButton();
        fernRadio.setText(" FernFlower (from jetbrains/intellij-community)");
        masterPanel.add(fernRadio, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        actionLabel = new JLabel();
        actionLabel.setText("ACTION");
        masterPanel.add(actionLabel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        jarLabel = new JLabel();
        jarLabel.setText("DECOMPILE JAR/DIR");
        masterPanel.add(jarLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        startBtn = new JButton();
        startBtn.setText("START EXPORT");
        masterPanel.add(startBtn, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        noteLabel = new JLabel();
        noteLabel.setText("说明");
        masterPanel.add(noteLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        noteValLabel = new JLabel();
        noteValLabel.setText("换行分割导出多个 JAR / 输入目录反编译导出内部所有 JAR ");
        masterPanel.add(noteValLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jarsScroll = new JScrollPane();
        masterPanel.add(jarsScroll, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        jarsText = new JTextArea();
        jarsText.setLineWrap(false);
        jarsText.setRows(5);
        jarsScroll.setViewportView(jarsText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return masterPanel;
    }

}
