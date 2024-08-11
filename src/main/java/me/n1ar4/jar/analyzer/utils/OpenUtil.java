package me.n1ar4.jar.analyzer.utils;

import me.n1ar4.jar.analyzer.gui.util.LogUtil;

public class OpenUtil {
    public static void open(String absPath) {
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

    private static void exec(String[] cmdArray) {
        try {
            String cmd = String.join(" ", cmdArray);
            LogUtil.info(String.format("run cmd: %s", cmd));
            new ProcessBuilder(cmdArray).start();
        } catch (Exception ignored) {
        }
    }
}
