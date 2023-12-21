package me.n1ar4.jar.analyzer.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "run y4lang script")
public class Y4LangCmd {
    @Parameter(names = {"-f", "--file"}, description = "y4lang .h file")
    private String file;

    public String getFile() {
        return file;
    }
}
