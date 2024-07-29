package me.n1ar4.jar.analyzer.sca;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.gui.util.LogUtil;
import me.n1ar4.jar.analyzer.utils.OSUtil;
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
        if (OSUtil.isWindows()) {
            String cmd = String.format("start %s", absPath);
            String[] xrayCmd = new String[]{"cmd.exe", "/c", String.format("%s", cmd)};
            exec(xrayCmd);
        } else {
            String cmd = String.format("open %s", absPath);
            String[] xrayCmd = new String[]{"/bin/bash", "-c", String.format("%s", cmd)};
            exec(xrayCmd);
        }
    }

    public static Process exec(String[] cmdArray) {
        try {
            String cmd = String.join(" ", cmdArray);
            LogUtil.info(String.format("run cmd: %s", cmd));
            return new ProcessBuilder(cmdArray).start();
        } catch (Exception ignored) {
        }
        return null;
    }
}
