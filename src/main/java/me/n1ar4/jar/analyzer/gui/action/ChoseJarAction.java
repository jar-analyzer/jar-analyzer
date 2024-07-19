package me.n1ar4.jar.analyzer.gui.action;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class ChoseJarAction {
    public static void run() {
        MainForm.getInstance().getChoseBtn().addActionListener(e -> {
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
            int option = fileChooser.showOpenDialog(new JFrame());
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String absPath = file.getAbsolutePath();
                LogUtil.info("load file: " + absPath);
                MainForm.getInstance().getFileText().setText(absPath);
            }
        });
    }
}
