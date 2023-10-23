package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.dto.ClassResult;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.env.Const;

public class ClassJDBCTest {
    public static void main(String[] args) {
        ConfigFile configFile = new ConfigFile();
        configFile.setDbPath(Const.dbFile);
        CoreEngine coreEngine = new CoreEngine(configFile);
        ClassResult c = coreEngine.getClassByClass("com/mysql/cj/jdbc/BlobFromLocator");
        System.out.println(c);
    }
}
