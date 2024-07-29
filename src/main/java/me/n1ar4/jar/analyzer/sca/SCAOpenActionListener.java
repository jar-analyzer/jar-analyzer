package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.sca.log.SCALogger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SCAOpenActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
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
                return "jar/war/dir";
            }
        });
        int option = fileChooser.showOpenDialog(new JFrame());
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String absPath = file.getAbsolutePath();
            SCALogger.logger.info("LOAD: " + absPath);
            MainForm.getInstance().getScaFileText().setText(absPath);
        }
    }
}
