package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;

public class Version {
    private static final Logger logger = LogManager.getLogger();

    public static boolean isJava8() {
        String version = System.getProperty("java.version");
        return version.startsWith("1.8");
    }

    public static void check() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.8")) {
            String[] versionComponents = version.split("_");
            if (versionComponents.length > 1) {
                try {
                    int updateVersion = Integer.parseInt(versionComponents[1]);
                    if (updateVersion <= 191) {
                        logger.warn("risk - java version is lower than 191");
                        JOptionPane.showMessageDialog(null,
                                "<html>vulnerability in versions lower than Java <strong>8u191</strong><br>" +
                                        "please use a higher version<br>" +
                                        "Java 版本小于 <strong>8u191</strong> 可能存在某些安全漏洞<br>" +
                                        "建议使用高于 <strong>8u191</strong> 版本的 Java<br>" +
                                        "该消息只是一个提示（点击确认正常启动）" +
                                        "</html>");
                    } else {
                        logger.info("safe - java version is higher than 191");
                    }
                } catch (NumberFormatException e) {
                    logger.warn("error java update version {}", versionComponents[1]);
                }
            } else {
                logger.warn("error java version {}", version);
            }
        } else {
            logger.warn("please use java 8 version");
            JOptionPane.showMessageDialog(null,
                    "<html>java <strong>8</strong> is recommended<br>your version is <strong>"
                            + version + "</strong><br>" +
                            "推荐使用 java <strong>8</strong> 版本<br>你使用的版本是 <strong>"
                            + version + "</strong><br>" +
                            "该消息只是一个提示（点击确认正常启动）" +
                            "</html>");
        }
    }
}
