/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.index;

import me.n1ar4.jar.analyzer.engine.index.IndexPluginsSupport;
import me.n1ar4.jar.analyzer.engine.index.entity.Result;

import java.io.IOException;


public class IndexPluginsSupportTest {
    public static void main(String[] args) throws Exception {
        boolean support = initIndex();
        if (support) {
            Result result = search();
            System.out.println(result);
        } else {
            System.out.println("test failed");
        }
        System.exit(0);
    }

    public static boolean initIndex() throws IOException, InterruptedException {
        return IndexPluginsSupport.initIndex();
    }

    public static Result search() throws Exception {
        return IndexPluginsSupport.search("[*] agent password");
    }
}
