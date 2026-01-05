/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.leak;

import me.n1ar4.jar.analyzer.leak.JDBCRule;

import java.util.List;

public class TestJDBC {
    public static void main(String[] args) {
        String input1 = "jdbc:mysql://localhost:3306/mydb?user=myuser&password=mypass";
        String input2 = "jdbc:postgresql://example.com:5432/mydb?sslmode=require";
        String input3 = "jdbc:oracle:thin:@localhost:1521:orcl";
        String input4 = "This is not a valid JDBC URL.";

        System.out.println("JDBC URLs found in input 1:");
        List<String> results1 = JDBCRule.match(input1);
        for (String url : results1) {
            System.out.println(url);
        }

        System.out.println("\nJDBC URLs found in input 2:");
        List<String> results2 = JDBCRule.match(input2);
        for (String url : results2) {
            System.out.println(url);
        }

        System.out.println("\nJDBC URLs found in input 3:");
        List<String> results3 = JDBCRule.match(input3);
        for (String url : results3) {
            System.out.println(url);
        }

        System.out.println("\nJDBC URLs found in input 4:");
        List<String> results4 = JDBCRule.match(input4);
        for (String url : results4) {
            System.out.println(url);
        }
    }
}
