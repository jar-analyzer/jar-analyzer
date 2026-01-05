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

public class EmailLeak {
    private static final String localEmail = "user.name@example.com";

    public static void testLeak() {
        System.out.println(localEmail);
        System.out.println("user-name@sub.domain.org");
    }
}
