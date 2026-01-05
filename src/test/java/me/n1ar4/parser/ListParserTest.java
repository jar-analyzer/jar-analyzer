/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.parser;

import me.n1ar4.jar.analyzer.gui.util.ListParser;

import java.util.List;

public class ListParserTest {
    public static void main(String[] args) {
        List<?> l = ListParser.parse("com.a.;com.b.T;");
        System.out.println(l);
        l = ListParser.parse("com.a.\ncom.b.T;");
        System.out.println(l);
        l = ListParser.parse("//test\ncom.a.\n#test\ncom.b.T;com.b.S\ncom.c.");
        System.out.println(l);
    }
}
