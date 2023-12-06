package me.n1ar4.jar.analyzer.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "build database")
public class BuildCmd {
    @Parameter(names = {"-j", "--jar"}, description = "jar file/dir")
    private String jar;
    @Parameter(names = {"--del-exist"}, description = "delete old database")
    private boolean delExist;
    @Parameter(names = {"--del-cache"}, description = "delete old cache")
    private boolean delCache;

    public String getJar() {
        return jar;
    }

    public boolean delExist() {
        return delExist;
    }

    public boolean delCache() {
        return delCache;
    }
}
