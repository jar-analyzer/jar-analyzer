/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.test;

public class FilePathLeak {
    private final static String path = "C:\\Users\\Public\\Documents\\file.txt";

    public static void test() {
        System.out.println(path);
        System.out.println("D:\\Program Files\\MyApp\\");
    }
}
