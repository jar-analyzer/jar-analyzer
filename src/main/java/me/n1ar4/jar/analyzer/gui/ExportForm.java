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

import me.n1ar4.jar.analyzer.engine.DecompileEngine;
import me.n1ar4.jar.analyzer.gui.util.ProcessDialog;
import me.n1ar4.jar.analyzer.gui.util.SwingLayout;
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
        initializeComponents();
    }

    private void initializeComponents() {
        masterPanel = new JPanel();
        SwingLayout.configureGrid(masterPanel, 6, 2, new Insets(5, 5, 5, 5), -1, -1);
        outputDirLabel = new JLabel();
        outputDirLabel.setText("OUTPUT DIR");
        SwingLayout.add(masterPanel, outputDirLabel, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        final Component spacer1 = Box.createGlue();
        SwingLayout.add(masterPanel, spacer1, 5, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, false, true, null, null, null, 0);
        outputDirText = new JTextField();
        SwingLayout.add(masterPanel, outputDirText, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, true, false, null, new Dimension(150, -1), null, 0);
        engineLabel = new JLabel();
        engineLabel.setText("ENGINE");
        SwingLayout.add(masterPanel, engineLabel, 3, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        fernRadio = new JRadioButton();
        fernRadio.setText(" FernFlower (from jetbrains/intellij-community)");
        SwingLayout.add(masterPanel, fernRadio, 3, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, true, false, null, null, null, 0);
        actionLabel = new JLabel();
        actionLabel.setText("ACTION");
        SwingLayout.add(masterPanel, actionLabel, 4, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        jarLabel = new JLabel();
        jarLabel.setText("DECOMPILE JAR/DIR");
        SwingLayout.add(masterPanel, jarLabel, 1, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        startBtn = new JButton();
        startBtn.setText("START EXPORT");
        SwingLayout.add(masterPanel, startBtn, 4, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, true, false, null, null, null, 0);
        noteLabel = new JLabel();
        noteLabel.setText("说明");
        SwingLayout.add(masterPanel, noteLabel, 2, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 1);
        noteValLabel = new JLabel();
        noteValLabel.setText("换行分割导出多个 JAR / 输入目录反编译导出内部所有 JAR ");
        SwingLayout.add(masterPanel, noteValLabel, 2, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, false, false, null, null, null, 0);
        jarsScroll = new JScrollPane();
        SwingLayout.add(masterPanel, jarsScroll, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, true, true, null, null, null, 0);
        jarsText = new JTextArea();
        jarsText.setLineWrap(false);
        jarsText.setRows(5);
        jarsScroll.setViewportView(jarsText);
    }

    /**
     * @noinspection ALL
     */
    public JComponent getRootComponent() {
        return masterPanel;
    }

}
