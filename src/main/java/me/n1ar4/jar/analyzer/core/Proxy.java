/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.core;

public class Proxy {
    private static final String SYSTEM_PROXY = "java.net.useSystemProxies";
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String HTTPS_PROXY_HOST = "https.proxyHost";
    private static final String HTTPS_PROXY_PORT = "https.proxyPort";
    private static final String SOCKS_PROXY_HOST = "socksProxyHost";
    private static final String SOCKS_PROXY_PORT = "socksProxyPort";

    public static boolean isSystemProxyOpen() {
        String data = System.getProperty(SYSTEM_PROXY);
        if (data == null || data.isEmpty()) {
            return false;
        }
        return data.equalsIgnoreCase("true");
    }

    public static void setSystemProxy() {
        System.setProperty(SYSTEM_PROXY, "true");
    }

    public static String getHttpHost() {
        return System.getProperty(HTTP_PROXY_HOST);
    }

    public static String getHttpPort() {
        return System.getProperty(HTTP_PROXY_PORT);
    }

    public static String getHttpsHost() {
        return System.getProperty(HTTPS_PROXY_HOST);
    }

    public static String getHttpsPort() {
        return System.getProperty(HTTPS_PROXY_PORT);
    }

    public static void setHttpProxy(String host, String port, boolean https) {
        System.setProperty(HTTP_PROXY_HOST, host);
        System.setProperty(HTTP_PROXY_PORT, port);
        if (https) {
            System.setProperty(HTTPS_PROXY_HOST, host);
            System.setProperty(HTTPS_PROXY_PORT, port);
        }
    }

    public static String getSocksProxyHost() {
        return System.getProperty(SOCKS_PROXY_HOST);
    }

    public static String getSocksProxyPort() {
        return System.getProperty(SOCKS_PROXY_PORT);
    }

    public static void setSocks(String ip, String port) {
        System.setProperty(SOCKS_PROXY_HOST, ip);
        System.setProperty(SOCKS_PROXY_PORT, port);
    }
}
