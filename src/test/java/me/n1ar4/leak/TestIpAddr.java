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

import me.n1ar4.jar.analyzer.leak.IPAddressRule;

import java.util.List;

public class TestIpAddr {
    public static void main(String[] args) {
        String validInput1 = "My IP address is 192.168.1.100, and your IP address is 10.0.0.1.";
        String validInput2 = "This is a valid IPv4 address: 127.0.0.1.";
        String invalidInput1 = "This is not a valid IP address: 300.0.0.1.";
        String invalidInput2 = "This is not a valid IP address: 10.0.0.";

        System.out.println("Valid Input 1:");
        List<String> validResults1 = IPAddressRule.match(validInput1);
        for (String ipAddress : validResults1) {
            System.out.println(ipAddress);
        }

        System.out.println("\nValid Input 2:");
        List<String> validResults2 = IPAddressRule.match(validInput2);
        for (String ipAddress : validResults2) {
            System.out.println(ipAddress);
        }

        System.out.println("\nInvalid Input 1:");
        List<String> invalidResults1 = IPAddressRule.match(invalidInput1);
        for (String ipAddress : invalidResults1) {
            System.out.println(ipAddress);
        }

        System.out.println("\nInvalid Input 2:");
        List<String> invalidResults2 = IPAddressRule.match(invalidInput2);
        for (String ipAddress : invalidResults2) {
            System.out.println(ipAddress);
        }
    }
}
