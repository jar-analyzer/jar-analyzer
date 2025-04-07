/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.rule;

import java.sql.DriverManager;

public class Test {
    public static void main(String[] args) throws Exception {
        DriverManager.getConnection("test");
        DriverManager.getDriver("test");
        // INVOKESTATIC java/sql/DriverManager.getConnection (Ljava/lang/String;)Ljava/sql/Connection;
        // INVOKESTATIC java/sql/DriverManager.getDriver (Ljava/lang/String;)Ljava/sql/Driver;
    }
}
