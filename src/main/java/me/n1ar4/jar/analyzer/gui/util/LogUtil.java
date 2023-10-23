package me.n1ar4.jar.analyzer.gui.util;

import javax.swing.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {
    private static JTextArea t;

    public static void setT(JTextArea t) {
        LogUtil.t = t;
    }

    public static void log(String msg) {
        if (t == null) {
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = LocalTime.now().format(formatter);
        String logStr = String.format("[log] [%s] %s\n", formattedTime, msg);
        t.append(logStr);
        t.setCaretPosition(t.getDocument().getLength());
    }
}
