package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.starter.Const;

import java.util.List;

public class MethodJDBCTest {
    public static void main(String[] args) {
        ConfigFile configFile = new ConfigFile();
        configFile.setDbPath(Const.dbFile);
        CoreEngine coreEngine = new CoreEngine(configFile);
        List<MethodResult> methods = coreEngine.getMethod(null,"parse",null);
        for (MethodResult m : methods) {
            System.out.println(m);
        }
    }
}
