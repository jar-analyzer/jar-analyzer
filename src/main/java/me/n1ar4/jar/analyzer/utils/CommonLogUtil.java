/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommonLogUtil {
    public static void log(String info, String suffix) {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss"));
        String datestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String logInfo = String.format("[%s] %s", timestamp, info);
        try {
            File logDirectory = new File("logs");
            if (!logDirectory.exists()) {
                boolean success = logDirectory.mkdirs();
                if (!success) {
                    throw new RuntimeException("make dirs error");
                }
            }
            File logFile = new File(logDirectory, datestamp + "-" + suffix + ".log");
            try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
                out.println(logInfo);
            }
        } catch (IOException e) {
            throw new RuntimeException("cannot write log file");
        }
    }
}
