package me.n1ar4.log;

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
            String className = LogUtil.getClassName();
            String methodName = LogUtil.getMethodName();
            String lineNumber = LogUtil.getLineNumber();
            String logMessage = String.format("[%s] [%s%s%s] [%s:%s:%s] %s",
                    timestamp, color, level.name(), ANSI_RESET, className,
                    methodName, lineNumber, message);
            System.out.println(logMessage);
        }
    }
}