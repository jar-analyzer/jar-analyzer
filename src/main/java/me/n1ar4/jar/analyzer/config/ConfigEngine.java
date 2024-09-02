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

package me.n1ar4.jar.analyzer.config;

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigEngine {
    private static final Logger logger = LogManager.getLogger();
    public static final String CONFIG_FILE_PATH = ".jar-analyzer";

    public static boolean exist() {
        Path configPath = Paths.get(CONFIG_FILE_PATH);
        return Files.exists(configPath);
    }

    public static ConfigFile parseConfig() {
        try {
            Path configPath = Paths.get(CONFIG_FILE_PATH);
            if (!Files.exists(configPath)) {
                return null;
            }

            byte[] data = Files.readAllBytes(configPath);
            if (new String(data).contains("!!me.n1ar4.")) {
                JOptionPane.showMessageDialog(null,
                        "<html>" +
                                "<p>config file <b>changed</b> in <b>2.5-beta+</b></p>" +
                                "<br>" +
                                "<p>the old config file will be deleted</p>" +
                                "</html>");
                Files.delete(configPath);
                return null;
            }

            Properties properties = new Properties();
            properties.load(new ByteArrayInputStream(Files.readAllBytes(configPath)));
            ConfigFile obj = new ConfigFile();
            obj.setDbPath(properties.getProperty("db-path"));
            obj.setDbSize(properties.getProperty("db-size"));
            obj.setJarPath(properties.getProperty("jar-path"));
            obj.setTempPath(properties.getProperty("temp-path"));
            obj.setTotalClass(properties.getProperty("total-class"));
            obj.setTotalJar(properties.getProperty("total-jar"));
            obj.setTotalMethod(properties.getProperty("total-method"));
            obj.setLang(properties.getProperty("lang"));
            return obj;
        } catch (Exception ex) {
            logger.error("parse config error: {}", ex.toString());
        }
        return null;
    }

    public static void saveConfig(ConfigFile configFile) {
        try {
            Path configPath = Paths.get(CONFIG_FILE_PATH);
            Properties properties = new Properties();
            properties.setProperty("db-path", configFile.getDbPath());
            properties.setProperty("db-size", configFile.getDbSize());
            properties.setProperty("jar-path", configFile.getJarPath());
            properties.setProperty("temp-path", configFile.getTempPath());
            properties.setProperty("total-class", configFile.getTotalClass());
            properties.setProperty("total-jar", configFile.getTotalJar());
            properties.setProperty("total-method", configFile.getTotalMethod());
            properties.setProperty("lang", configFile.getLang());
            properties.store(Files.newOutputStream(configPath), null);
        } catch (Exception ex) {
            logger.error("save config error: {}", ex.toString());
        }
    }
}
