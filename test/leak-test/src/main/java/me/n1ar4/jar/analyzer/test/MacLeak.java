/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.test;

public class MacLeak {
    private static final String test = "00:1A:2B:3C:4D:5E";

    public static void testLeak() {
        System.out.println(test);
        System.out.println("FF:EE:DD:CC:BB:AA");
    }
}
