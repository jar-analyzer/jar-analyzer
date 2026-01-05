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

public class JDBCLeak {
    private static final String test = "jdbc:mysql://localhost:3306/mydatabase";

    public static void testLeak() {
        System.out.println(test);
        System.out.println("jdbc:postgresql://user:password@localhost:5432/mydb");
    }
}
