package me.n1ar4.jar.analyzer.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "start jar-analyzer gui")
public class StartCmd {
    public static final String CMD = "gui";

    @Parameter(names = {"-p", "--port"}, description = "server port")
    public int port;

    @Parameter(names = {"-t", "--theme"},
            description = "use theme name (default|metal|win|win-classic|motif|mac|gtk|cross|aqua|nimbus)")
    public String theme;

    public StartCmd() {

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
