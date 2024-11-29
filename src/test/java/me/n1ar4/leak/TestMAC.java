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
