/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent;

public class Main {
    public static void main(String[] args) {
        System.out.println("##################################################################");
        System.out.println("THIS IS AN PREMAIN JAVA-AGENT");
        System.out.println("usage: -javaagent:agent.jar=port=[port];password=[password]");
        System.out.println("##################################################################");
    }
}