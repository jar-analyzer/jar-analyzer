package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.config.ConfigFile;
import me.n1ar4.jar.analyzer.dto.MethodResult;
import me.n1ar4.jar.analyzer.engine.CoreEngine;
import me.n1ar4.jar.analyzer.env.Const;

import java.util.ArrayList;

public class ImplJdbcTest {
    public static void main(String[] args) {
        ConfigFile configFile = new ConfigFile();
        configFile.setDbPath(Const.dbFile);
        CoreEngine coreEngine = new CoreEngine(configFile);
        ArrayList<MethodResult> c = coreEngine.getImpls(
                "com/google/protobuf/ApiOrBuilder",
                "getOptionsCount",
                "()I");
        System.out.println(c);
    }
}
