package me.n1ar4.bcel;

import org.apache.bcel.classfile.Utility;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Test {
    public static void main(String[] args) throws Exception {
        byte[] data = Files.readAllBytes(Paths.get(
                "target/classes/me/n1ar4/jar/analyzer/plugins/bcel/HelloWorld.class"));
        String bcel = Utility.encode(data, true);
        System.out.println("$$BCEL$$" + bcel);
    }
}
