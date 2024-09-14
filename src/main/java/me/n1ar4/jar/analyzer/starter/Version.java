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
                        logger.debug("safe - java version is higher than 191");
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
