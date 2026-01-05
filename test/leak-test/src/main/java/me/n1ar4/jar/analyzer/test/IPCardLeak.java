/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.test;

public class IPCardLeak {
    private static final String test = "123456789012345678";

    public static void testLeak() {
        System.out.println(test);
        System.out.println("123456781234567");
    }
}
