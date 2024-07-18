package me.n1ar4.jar.analyzer.gui.util;

import cn.hutool.core.util.StrUtil;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {
    private static JTextPane t;
    private static Style styleRed;
    private static Style styleGreen;

    public static void setT(JTextPane t) {
        LogUtil.t = t;
        if(styleRed==null||styleGreen==null){
            styleRed = t.getStyledDocument().addStyle("RedStyle", null);
            StyleConstants.setForeground(styleRed, Color.red);
            styleGreen = t.getStyledDocument().addStyle("BlueStyle", null);
            StyleConstants.setForeground(styleGreen, Color.green);
        }
    }

    public static void info(String msg) {
        print(styleGreen,msg);
    }

    private static void print(Style style,String msg) {
        if (t == null) {
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = LocalTime.now().format(formatter);
        String head;
        if(style==styleRed){
            head="error";
        }
        else if(style==styleGreen){
            head="info";
        }
        else {
            head="unknown";
        }
        String logStr = StrUtil.format("[{}] [{}] {}\n",head, formattedTime, msg);
        try {
            t.getStyledDocument().insertString(t.getStyledDocument().getLength(), logStr, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
        t.setCaretPosition(t.getDocument().getLength());
    }

    public static void error(String msg) {
        print(styleRed,msg);
    }
}
