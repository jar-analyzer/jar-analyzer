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

import me.n1ar4.jar.analyzer.leak.MacAddressRule;

import java.util.List;

public class TestMAC {
    public static void main(String[] args) {
        String input1 = "My MAC address is 00:11:22:33:44:55, and your MAC address is 66:77:88:99:aa:bb.";
        String input2 = "This is not a valid MAC address: 00-11-22-33-44-56.";
        String input3 = "The MAC address is 00:11:22:33:44:55 and the serial number is 12345678.";

        System.out.println("MAC addresses found in input 1:");
        List<String> results1 = MacAddressRule.match(input1);
        for (String macAddress : results1) {
            System.out.println(macAddress);
        }

        System.out.println("\nMAC addresses found in input 2:");
        List<String> results2 = MacAddressRule.match(input2);
        for (String macAddress : results2) {
            System.out.println(macAddress);
        }

        System.out.println("\nMAC addresses found in input 3:");
        List<String> results3 = MacAddressRule.match(input3);
        for (String macAddress : results3) {
            System.out.println(macAddress);
        }
    }
}
