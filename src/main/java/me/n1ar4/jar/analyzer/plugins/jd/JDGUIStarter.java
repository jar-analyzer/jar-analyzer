package me.n1ar4.jar.analyzer.plugins.jd;

import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.jar.analyzer.utils.OSUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JDGUIStarter {
    private static final Logger logger = LogManager.getLogger();

    public static String jdGUIFileName = "jd-gui-1.6.6.jar";

    public static void start() {
        String javaHome = System.getProperty("java.home");
        Path java;
        if (OSUtil.isWindows()) {
            java = Paths.get(javaHome, "bin", "java.exe");
        } else {
            java = Paths.get(javaHome, "bin", "java");
        }
        Path jd = Paths.get("lib", jdGUIFileName);

        if (!Files.exists(jd)) {
            jd = Paths.get(jdGUIFileName);
            if (!Files.exists(jd)) {
                logger.warn("{} not found",jdGUIFileName);
                logger.warn("{} should be in current dir or lib dir",jdGUIFileName);
                JOptionPane.showMessageDialog(MainForm.getInstance().getMasterPanel(),
                        "JD-GUI 文件找不到（请查看日志信息）");
                return;
            }
        }

        String input = MainForm.getInstance().getFileText().getText().trim();
        if (input.isEmpty()) {
            logger.info("input jar is null");
        }

        List<String> cmdList = new ArrayList<>();
        cmdList.add(java.toAbsolutePath().toString());
        cmdList.add("-jar");
        cmdList.add(jd.toAbsolutePath().toString());
        if (!input.isEmpty()) {
            cmdList.add(input);
        }

        String[] cmdArray = cmdList.toArray(new String[0]);
        logger.info("start jd-gui : {}", Arrays.toString(cmdArray));
        ProcessBuilder pb = new ProcessBuilder(cmdArray);
        try {
            pb.start();
        } catch (IOException e) {
            logger.error("start jd-gui error: {}",e.getMessage());
        }
    }
}
