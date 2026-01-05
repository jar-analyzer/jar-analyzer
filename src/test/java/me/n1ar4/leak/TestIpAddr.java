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
