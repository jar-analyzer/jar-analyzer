/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
    private static Style styleYellow;

    public static void setT(JTextPane t) {
        LogUtil.t = t;
        if (styleRed == null || styleGreen == null || styleYellow == null) {
            styleRed = t.getStyledDocument().addStyle("RedStyle", null);
            StyleConstants.setForeground(styleRed, Color.red);
            styleGreen = t.getStyledDocument().addStyle("BlueStyle", null);
            StyleConstants.setForeground(styleGreen, Color.green);
            styleYellow = t.getStyledDocument().addStyle("YellowStyle", null);
            StyleConstants.setForeground(styleYellow, Color.yellow);
        }
    }

    private static void print(Style style, String msg) {
        if (t == null) {
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = LocalTime.now().format(formatter);
        String head;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 3) {
            head = stackTrace[2].getMethodName();
        } else {
            head = "Unknown Level";
        }
        String logStr = StrUtil.format("[{}] [{}] {}\n", head, formattedTime, msg);
        try {
            t.getStyledDocument().insertString(t.getStyledDocument().getLength(), logStr, style);
        } catch (Exception ignored) {
        }
        t.setCaretPosition(t.getDocument().getLength());
    }

    public static void info(String msg) {
        print(styleGreen, msg);
    }

    public static void warn(String msg) {
        print(styleYellow, msg);
    }

    public static void error(String msg) {
        print(styleRed, msg);
    }
}
