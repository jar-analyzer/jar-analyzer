package me.n1ar4.security;

import me.n1ar4.jar.analyzer.utils.ColorUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SecurityLog {
    static void log(String info) {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logInfo = String.format("[%s] [%s] %s\n",
                timestamp, ColorUtil.red("SECURITY"), info);
        System.out.print(logInfo);
    }
}
