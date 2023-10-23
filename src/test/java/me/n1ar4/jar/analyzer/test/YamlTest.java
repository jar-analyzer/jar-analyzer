package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.config.ConfigEngine;
import me.n1ar4.jar.analyzer.config.ConfigFile;

public class YamlTest {
    public static void main(String[] args) {
        ConfigFile configFile = new ConfigFile();
        configFile.setDbPath("1");
        configFile.setDbSize("1");
        configFile.setJarPath("1");
        configFile.setTempPath("1");
        configFile.setTotalClass("1");
        configFile.setTotalJar("1");
        configFile.setTotalMethod("1");
        ConfigEngine.saveConfig(configFile);

        ConfigFile c = ConfigEngine.parseConfig();
        System.out.println(c);
    }
}
