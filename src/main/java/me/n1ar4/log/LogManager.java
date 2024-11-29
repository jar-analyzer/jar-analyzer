/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.log;

public class LogManager {
    static LogLevel logLevel = LogLevel.INFO;

    public static void setLevel(LogLevel level) {
        logLevel = level;
    }

    public static Logger getLogger() {
        return new Logger();
    }
}
