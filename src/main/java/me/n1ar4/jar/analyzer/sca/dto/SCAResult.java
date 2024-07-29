package me.n1ar4.jar.analyzer.sca.dto;

public class SCAResult {
    private String jarPath;
    private String keyClass;
    private String hash;
    private String project;
    private String version;
    private String CVE;

    public String getCVE() {
        return CVE;
    }

    public void setCVE(String CVE) {
        this.CVE = CVE;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getKeyClass() {
        return keyClass;
    }

    public void setKeyClass(String keyClass) {
        this.keyClass = keyClass;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SCAResult{" +
                "hash='" + hash + '\'' +
                ", project='" + project + '\'' +
                ", CVE='" + CVE + '\'' +
                '}';
    }
}
