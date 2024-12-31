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

public class UrlLeak {
    private static final String leakUrl = "https://example.com";
    private static final String localUrl = "http://localhost:12345";

    public static void testLeak() {
        // test https
        System.out.println(leakUrl);
        // test http
        System.out.println(localUrl);
        // test ldc
        System.out.println("https://github.com/jar-analyzer");
    }
}
