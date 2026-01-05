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

import me.n1ar4.jar.analyzer.leak.EmailRule;

import java.util.List;

public class TestEmail {
    public static void main(String[] args) {
        String text = "Contact us at support@example.com or sales@my-site.org!";
        List<String> emails = EmailRule.match(text);
        System.out.println("Matched Emails: " + emails);
    }
}
