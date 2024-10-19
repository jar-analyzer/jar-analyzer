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
