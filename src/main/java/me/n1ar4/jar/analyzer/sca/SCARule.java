package me.n1ar4.jar.analyzer.sca;

/**
 * 该类是 SCA 匹配 CVE 的规则
 */
public class SCARule {
    // 独立的规则 ID
    private String uuid;
    // CVE-ID
    private String CVE;
    // 项目名称 例如 Apache Log4j2
    private String projectName;
    // 存在该漏洞的版本信息
    private String version;
    // 关键 CLASS 全名 例如 org.apache.A
    private String keyClassName;
    // 关键 CLASS 的 HASH 如果可以匹配成功 认为存在该漏洞
    private String hash;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCVE() {
        return CVE;
    }

    public void setCVE(String CVE) {
        this.CVE = CVE;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKeyClassName() {
        return keyClassName;
    }

    public void setKeyClassName(String keyClassName) {
        this.keyClassName = keyClassName;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
