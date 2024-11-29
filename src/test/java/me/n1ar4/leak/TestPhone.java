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

import me.n1ar4.jar.analyzer.leak.PhoneRule;

import java.util.List;

public class TestPhone {
    public static void main(String[] args) {
        String input1 = "My phone number is +86 138 1234 5678, and your phone number is 00861391234567.";
        String input2 = "This is not a valid phone number: 123456789.";
        String input3 = "The phone number is 13812345678 and the email is example@example.com.";

        System.out.println("Phone numbers found in input 1:");
        List<String> results1 = PhoneRule.match(input1);
        for (String phoneNumber : results1) {
            System.out.println(phoneNumber);
        }

        System.out.println("\nPhone numbers found in input 2:");
        List<String> results2 = PhoneRule.match(input2);
        for (String phoneNumber : results2) {
            System.out.println(phoneNumber);
        }

        System.out.println("\nPhone numbers found in input 3:");
        List<String> results3 = PhoneRule.match(input3);
        for (String phoneNumber : results3) {
            System.out.println(phoneNumber);
        }
    }
}
