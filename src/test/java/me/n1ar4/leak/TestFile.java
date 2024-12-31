/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.leak;

import me.n1ar4.jar.analyzer.leak.FilePathRule;

import java.util.List;

public class TestFile {
    public static void main(String[] args) {
        String input = "Here are some paths: C:\\Users\\Public\\Documents\\file.txt and D:\\Program Files\\MyApp\\test.asp \nInvalid Path: D:\\Invalid Path\\file.txt";        List<String> paths = FilePathRule.match(input);
        for (String path : paths) {
            System.out.println(path);
        }
    }
}
