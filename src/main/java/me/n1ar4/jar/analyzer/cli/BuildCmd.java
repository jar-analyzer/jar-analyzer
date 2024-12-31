/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "build database")
public class BuildCmd {
    public static final String CMD = "build";
    @Parameter(names = {"-j", "--jar"}, description = "jar file/dir")
    private String jar;
    @Parameter(names = {"--del-exist"}, description = "delete old database")
    private boolean delExist;
    @Parameter(names = {"--del-cache"}, description = "delete old cache")
    private boolean delCache;
    @Parameter(names = {"--inner-jars"}, description = "resolve jars in jar")
    private boolean innerJars;

    public BuildCmd() {

    }

    public String getJar() {
        return jar;
    }

    public boolean delExist() {
        return delExist;
    }

    public boolean delCache() {
        return delCache;
    }

    public boolean enableInnerJars() {
        return innerJars;
    }
}
