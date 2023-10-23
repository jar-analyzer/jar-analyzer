package me.n1ar4.jar.analyzer.test;


import me.n1ar4.jar.analyzer.asm.IdentifyCallEngine;

public class OpcodeTest {
    public static void main(String[] args)throws Exception {
        String test = IdentifyCallEngine.run("D:\\new-jar-analyzer\\jar-analyzer\\jar-analyzer-temp\\com\\mysql\\cj\\admin\\ServerController.class","stop","(Z)V");
        System.out.println(test);
    }
}
