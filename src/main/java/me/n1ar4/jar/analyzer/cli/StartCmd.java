package me.n1ar4.jar.analyzer.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "start jar-analyzer gui")
public class StartCmd {
    public static final String CMD = "gui";

    @Parameter(names = {"-p", "--port"}, description = "server port")
    public int port;

    public StartCmd() {

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
