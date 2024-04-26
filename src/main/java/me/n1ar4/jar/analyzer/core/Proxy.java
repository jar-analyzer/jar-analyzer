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
