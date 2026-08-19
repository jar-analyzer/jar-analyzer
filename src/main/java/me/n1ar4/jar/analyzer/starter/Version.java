/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.starter;

import me.n1ar4.jar.analyzer.utils.ColorUtil;
import me.n1ar4.jar.analyzer.utils.OSUtil;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;

public class Version {
    private static final Logger logger = LogManager.getLogger();

    @SuppressWarnings("all")
    public static void check() {
        String version = System.getProperty("java.version");

        // 2026/08/19
        // MACOS 系统的 AWT/SWING 在旧版 JDK 上无法正常工作，强制要求 JAVA 11+
        int majorVersion = getMajorVersion(version);
        if (OSUtil.isMac() && majorVersion > 0 && majorVersion < 11) {
            System.out.println(ColorUtil.red("###############################################"));
            System.out.println(ColorUtil.red("当前系统为 macOS，JDK 版本低于 11，禁止启动"));
            System.out.println(ColorUtil.red("current java version: " + version));
            System.out.println(ColorUtil.red("macOS 必须使用 JAVA 11 及以上版本启动"));
            System.out.println(ColorUtil.red("macOS requires Java 11 or later to run"));
            System.out.println(ColorUtil.red("###############################################"));
            logger.error("macOS requires Java 11 or later, current version: {}", version);
            try {
                JOptionPane.showMessageDialog(null,
                        String.format("current java version: %s%n%nmacOS " +
                                        "必须使用 JAVA 11 及以上版本启动%nmacOS requires Java 11 or later to run",
                                version),
                        "Jar Analyzer",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Throwable ignored) {
            }
            System.exit(1);
        }

        if (version.startsWith("1.8")) {
            String[] versionComponents = version.split("_");
            if (versionComponents.length > 1) {
                try {
                    int updateVersion = Integer.parseInt(versionComponents[1]);
                    if (updateVersion <= 191) {
                        logger.warn("risk - java version is lower than 191");
                    } else {
                        logger.debug("safe - java version is higher than 191");
                    }
                } catch (NumberFormatException e) {
                    logger.warn("error java update version {}", versionComponents[1]);
                }
            } else {
                logger.warn("error java version {}", version);
            }
        } else {
            logger.info("please use java 8 version");
        }
    }

    /**
     * 解析 JDK 主版本号
     * 兼容 1.8.0_302 / 11.0.2 / 17.0.1 / 11-ea 等格式
     * 解析失败返回 0（不拦截）
     */
    private static int getMajorVersion(String version) {
        try {
            String v = version.trim();
            if (v.startsWith("1.")) {
                v = v.substring(2);
            }
            int end = 0;
            while (end < v.length() && Character.isDigit(v.charAt(end))) {
                end++;
            }
            return end == 0 ? 0 : Integer.parseInt(v.substring(0, end));
        } catch (Exception e) {
            return 0;
        }
    }
}
