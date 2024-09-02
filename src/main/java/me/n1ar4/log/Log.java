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

package me.n1ar4.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Log {
    static final String ANSI_RESET = "\u001B[0m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_RED = "\u001B[31m";
    static final String ANSI_BLUE = "\u001B[34m";

    static void info(String message) {
        log(LogLevel.INFO, message, ANSI_GREEN);
    }

    static void error(String message) {
        log(LogLevel.ERROR, message, ANSI_RED);
    }

    static void debug(String message) {
        log(LogLevel.DEBUG, message, ANSI_BLUE);
    }

    static void warn(String message) {
        log(LogLevel.WARN, message, ANSI_YELLOW);
    }

    static void log(LogLevel level, String message, String color) {
        if (level.compareTo(LogManager.logLevel) >= 0) {
            String timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss"));
            String datestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String className = LogUtil.getClassName();
            String methodName = LogUtil.getMethodName();
            String lineNumber = LogUtil.getLineNumber();
            String logMessage;
            if (!lineNumber.equals("-1")) {
                logMessage = String.format("[%s] [%s%s%s] [%s:%s:%s] %s",
                        timestamp, color, level.name(), ANSI_RESET, className,
                        methodName, lineNumber, message);
            } else {
                logMessage = String.format("[%s] [%s%s%s] [%s:%s] %s",
                        timestamp, color, level.name(), ANSI_RESET, className,
                        methodName, message);
            }
            System.out.println(logMessage);

            try {
                File logDirectory = new File("logs");
                if (!logDirectory.exists()) {
                    boolean success = logDirectory.mkdirs();
                    if (!success) {
                        throw new RuntimeException("make dirs error");
                    }
                }
                File logFile = new File(logDirectory, datestamp + ".log");
                try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
                    String fileMessage;
                    if (!lineNumber.equals("-1")) {
                        fileMessage = String.format("[%s] [%s] [%s:%s:%s] %s",
                                timestamp, level.name(), className,
                                methodName, lineNumber, message);
                    } else {
                        fileMessage = String.format("[%s] [%s] [%s:%s] %s",
                                timestamp, level.name(), className,
                                methodName, message);
                    }
                    out.println(fileMessage);
                }
            } catch (IOException e) {
                throw new RuntimeException("cannot write log file");
            }
        }
    }
}