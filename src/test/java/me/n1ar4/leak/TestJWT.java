/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.leak;

import me.n1ar4.jar.analyzer.leak.JWTRule;

import java.util.List;

public class TestJWT {
    public static void main(String[] args) {
        String input1 = "This is a valid JWT token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String input2 = "This is another valid JWT token: eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJqb2UiLCJleHAiOjEzMDA4MTkzODAsImh0dHA6Ly9leGFtcGxlLmNvbS9pc19yb290Ijp0cnVlfQ.dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String input3 = "This is not a valid JWT token: This is a string that does not match the JWT format.";

        System.out.println("JWT tokens found in input 1:");
        List<String> results1 = JWTRule.match(input1);
        for (String token : results1) {
            System.out.println(token);
        }

        System.out.println("\nJWT tokens found in input 2:");
        List<String> results2 = JWTRule.match(input2);
        for (String token : results2) {
            System.out.println(token);
        }

        System.out.println("\nJWT tokens found in input 3:");
        List<String> results3 = JWTRule.match(input3);
        for (String token : results3) {
            System.out.println(token);
        }
    }
}
