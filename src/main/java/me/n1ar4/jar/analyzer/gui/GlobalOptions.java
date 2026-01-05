/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui;

import me.n1ar4.jar.analyzer.server.ServerConfig;

public class GlobalOptions {
    public static final int CHINESE = 1;
    public static final int ENGLISH = 2;
    private static int LANGUAGE;

    private static ServerConfig serverConfig;
    private static boolean securityMode;

    static {
        LANGUAGE = ENGLISH;
    }

    public static void setLang(int lang) {
        if (lang != CHINESE && lang != ENGLISH) {
            throw new RuntimeException("invalid language");
        }
        LANGUAGE = lang;
    }

    public static int getLang() {
        return LANGUAGE;
    }

    public static int getLANGUAGE() {
        return LANGUAGE;
    }

    public static void setLANGUAGE(int LANGUAGE) {
        GlobalOptions.LANGUAGE = LANGUAGE;
    }

    public static ServerConfig getServerConfig() {
        return serverConfig;
    }

    public static void setServerConfig(ServerConfig serverConfig) {
        GlobalOptions.serverConfig = serverConfig;
    }

    public static void setSecurity(boolean securityMode) {
        GlobalOptions.securityMode = securityMode;
    }

    public static boolean isSecurityMode() {
        return securityMode;
    }
}
