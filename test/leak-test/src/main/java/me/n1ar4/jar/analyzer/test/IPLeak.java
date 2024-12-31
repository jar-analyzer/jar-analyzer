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

public class IPLeak {
    private static final String test = "10.255.255.255";

    public static void testLeak() {
        System.out.println(test);
        System.out.println("192.168.255.255");
    }
}
