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

@Parameters(commandDescription = "start jar-analyzer gui")
public class StartCmd {
    public static final String CMD = "gui";

    @Parameter(names = {"-p", "--port"}, description = "server port")
    public int port;

    @Parameter(names = {"-fz", "--font-size"}, description = "font size")
    public int fontSize;

    @Parameter(names = {"-t", "--theme"},
            description = "use theme name (default|metal|win|win-classic|motif|mac|gtk|cross|aqua|nimbus)")
    public String theme;

    @Parameter(names = {"-l", "--log-level"}, description = "set log level (debug|info|warn|error)")
    public String logLevel;

    @Parameter(names = {"-n", "--no-check"}, description = "disable all update http request")
    public boolean noHttp;

    @Parameter(names = {"-sl", "--skip-load"}, description = "disable loading")
    public boolean skipLoad;

    public StartCmd() {

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getFontSize() {
        return fontSize;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public boolean isNoHttp() {
        return noHttp;
    }

    public boolean isSkipLoad() {
        return skipLoad;
    }
}
