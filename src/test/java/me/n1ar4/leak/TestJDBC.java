/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
