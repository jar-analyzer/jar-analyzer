package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.OpenUtil;
import me.n1ar4.jar.analyzer.utils.StringUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SCAOpenResultListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        MainForm instance = MainForm.getInstance();
        String text = instance.getOutputFileText().getText();
        if (text == null || StringUtil.isNull(text)) {
            JOptionPane.showMessageDialog(instance.getMasterPanel(),
                    "NO RESULT HTML FILE");
            return;
        }
        Path path = Paths.get(text);
        if (Files.notExists(path)) {
            JOptionPane.showMessageDialog(instance.getMasterPanel(),
                    "TARGET FILE NOT EXIST");
            return;
        }
        String absPath = path.toAbsolutePath().toString();
        if (absPath.trim().contains(" ")) {
            JOptionPane.showMessageDialog(instance.getMasterPanel(),
                    "PATH SHOULD NOT CONTAINS SPACE");
            return;
        }
        OpenUtil.open(absPath);
    }
}
