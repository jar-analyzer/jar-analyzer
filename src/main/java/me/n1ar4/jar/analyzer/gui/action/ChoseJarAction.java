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
import java.io.File;

public class ChoseJarAction {
    public static void run() {
        MainForm.getInstance().getChoseBtn().addActionListener(e -> {
            JDialog progressDialog = createProgressDialog();
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

    private static JDialog createProgressDialog() {
        Window owner = SwingUtilities.getWindowAncestor(MainForm.getInstance().getMasterPanel());
        JDialog dialog = new JDialog(owner);
        dialog.setTitle("提示");
        dialog.setModal(false);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JLabel label = new JLabel("<html>在最新 <b>Windows</b> 中 " +
                "<span style='color:#d35400'><b>Java 8</b></span> 打开文件浏览器效率很低，" +
                "建议使用 <b>Java 21/25</b> 有改进</html>");
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
