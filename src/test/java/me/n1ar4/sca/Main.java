/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.sca;

import me.n1ar4.jar.analyzer.sca.utils.SCAHashUtil;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 测试 Hash 使用
 */
public class Main {
    public static void main(String[] args) throws Exception {
        byte[] data = Files.readAllBytes(Paths.get("TypeUtils.class"));
        String hex = SCAHashUtil.sha256(data);
        System.out.println(hex);
    }
}
