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

public class JWTLeak {
    private static final String test = "eyJabc1234567890.def4567890.ghijklmnop";

    public static void testLeak() {
        System.out.println(test);
        System.out.println("eyJabc_def-1234567/890+xyz.1234567890.abc_def");
    }
}
