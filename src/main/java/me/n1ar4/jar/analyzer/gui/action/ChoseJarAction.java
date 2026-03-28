/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChoseJarAction {
    public static void run() {
        MainForm.getInstance().getChoseBtn().addActionListener(e -> {
            AtomicBoolean cancelled = new AtomicBoolean(false);
            JDialog progressDialog = createProgressDialog(cancelled);
            progressDialog.setVisible(true);
            new Thread(() -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooser.setFileHidingEnabled(false);
                fileChooser.setCurrentDirectory(new File("."));
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".jar") ||
                                f.getName().toLowerCase().endsWith(".war") ||
                                f.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "jar/war";
                    }
                });
                SwingUtilities.invokeLater(() -> {
                    progressDialog.dispose();
                    if (cancelled.get()) {
                        LogUtil.info("user cancelled file chooser");
                        return;
                    }
                    int option = fileChooser.showOpenDialog(MainForm.getInstance().getMasterPanel());
                    if (option == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        String absPath = file.getAbsolutePath();
                        LogUtil.info("load file: " + absPath);
                        MainForm.getInstance().getFileText().setText(absPath);
                    }
                });
            }).start();
        });
    }

    private static JDialog createProgressDialog(AtomicBoolean cancelled) {
        Window owner = SwingUtilities.getWindowAncestor(MainForm.getInstance().getMasterPanel());
        JDialog dialog = new JDialog(owner);
        dialog.setTitle("提示");
        dialog.setModal(false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelled.set(true);
            }
        });

        JLabel label = new JLabel("<html>在最新 <b>Windows</b> 中 " +
                "<span style='color:#d35400'><b>Java 8</b></span> 打开文件浏览器效率很低，" +
                "建议使用 <b>Java 21/25</b> 有改进" +
                "<p>关闭此窗口可取消打开 <b>jar-analyzer</b> 支持直接 <b>拖拽</b> 文件到窗口</p></html>");
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(label, BorderLayout.CENTER);
        panel.add(bar, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        return dialog;
    }
}
