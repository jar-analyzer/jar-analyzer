package me.n1ar4.jar.analyzer.config;

public class ConfigFile {
    private String jarPath;
    private String dbPath;
    private String tempPath;
    private String dbSize;
    private String totalJar;
    private String totalClass;
    private String totalMethod;
    private String gptHost;
    private String gptKey;
    private String gptProxyHost;
    private String gptProxyPort;

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public String getDbSize() {
        return dbSize;
    }

    public void setDbSize(String dbSize) {
        this.dbSize = dbSize;
    }

    public String getTotalJar() {
        return totalJar;
    }

    public void setTotalJar(String totalJar) {
        this.totalJar = totalJar;
    }

    public String getTotalClass() {
        return totalClass;
    }

    public void setTotalClass(String totalClass) {
        this.totalClass = totalClass;
    }

    public String getTotalMethod() {
        return totalMethod;
    }

    public void setTotalMethod(String totalMethod) {
        this.totalMethod = totalMethod;
    }

    public String getGptHost() {
        return gptHost;
    }

    public void setGptHost(String gptHost) {
        this.gptHost = gptHost;
    }

    public String getGptKey() {
        return gptKey;
    }

    public void setGptKey(String gptKey) {
        this.gptKey = gptKey;
    }

    public String getGptProxyHost() {
        return gptProxyHost;
    }

    public void setGptProxyHost(String gptProxyHost) {
        this.gptProxyHost = gptProxyHost;
    }

    public String getGptProxyPort() {
        return gptProxyPort;
    }

    public void setGptProxyPort(String gptProxyPort) {
        this.gptProxyPort = gptProxyPort;
    }

    @Override
    public String toString() {
        return "ConfigFile{" +
                "jarPath='" + jarPath + '\'' +
                ", dbPath='" + dbPath + '\'' +
                ", tempPath='" + tempPath + '\'' +
                ", dbSize='" + dbSize + '\'' +
                ", totalJar='" + totalJar + '\'' +
                ", totalClass='" + totalClass + '\'' +
                ", totalMethod='" + totalMethod + '\'' +
                ", gptHost='" + gptHost + '\'' +
                ", gptKey='" + gptKey + '\'' +
                ", gptProxyHost='" + gptProxyHost + '\'' +
                ", gptProxyPort='" + gptProxyPort + '\'' +
                '}';
    }
}
