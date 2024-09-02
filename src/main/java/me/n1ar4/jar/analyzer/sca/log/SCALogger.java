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

package me.n1ar4.jar.analyzer.sca.log;

import javax.swing.*;

public class SCALogger {
    public static SCALogger logger;
    private final JTextArea logArea;

    public SCALogger(JTextArea area) {
        this.logArea = area;
    }

    public void print(String s) {
        this.logArea.append(s);
        this.logArea.setCaretPosition(this.logArea.getDocument().getLength());
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
