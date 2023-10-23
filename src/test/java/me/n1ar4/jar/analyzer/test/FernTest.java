package me.n1ar4.jar.analyzer.test;

import me.n1ar4.jar.analyzer.decompile.DecompileEngine;

import java.nio.file.Paths;

public class FernTest {
    public static void main(String[] ignoredArg) {
        String data = DecompileEngine.decompile(Paths.get("C:\\Java\\jar-analyzer\\" +
                "target\\classes\\com\\intellij\\uiDesigner\\core\\AbstractLayout.class"));
        System.out.println(data);
    }
}
