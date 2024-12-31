/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.leak;

import me.n1ar4.jar.analyzer.leak.UrlRule;

import java.util.List;

public class TestUrl {
    public static void main(String[] args) {
        String input1 = "Check out my website at https://www.example.com, and visit https://www.google.com too.";
        String input2 = "This is not a valid URL: ftp://example.com/1/2/3";
        String input3 = "The URL is https://www.example.org/index.html, and the email is example@example.com.";

        System.out.println("URLs found in input 1:");
        List<String> results1 = UrlRule.match(input1);
        for (String url : results1) {
            System.out.println(url);
        }

        System.out.println("\nURLs found in input 2:");
        List<String> results2 = UrlRule.match(input2);
        for (String url : results2) {
            System.out.println(url);
        }

        System.out.println("\nURLs found in input 3:");
        List<String> results3 = UrlRule.match(input3);
        for (String url : results3) {
            System.out.println(url);
        }
    }
}
