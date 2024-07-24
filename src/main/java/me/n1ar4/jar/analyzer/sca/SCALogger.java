package me.n1ar4.jar.analyzer.sca;

import javax.swing.*;

public class SCALogger {
    public static SCALogger logger;
    private final JTextArea logArea;

    public SCALogger(JTextArea area) {
        this.logArea = area;
    }

    private void log(String level, String msg) {
        String logInfo = "[" + level + "] " + msg + "\n";
        this.logArea.append(logInfo);
        this.logArea.setCaretPosition(this.logArea.getDocument().getLength());
    }

    public void info(String msg) {
        log("INFO", msg);
    }

    public void warn(String msg) {
        log("WARN", msg);
    }

    public void error(String msg) {
        log("ERROR", msg);
    }
}
