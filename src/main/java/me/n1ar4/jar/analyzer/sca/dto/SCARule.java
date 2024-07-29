package me.n1ar4.jar.analyzer.sca.dto;

import java.util.Map;

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
    private Map<String, String> hashMap;

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

    public Map<String, String> getHashMap() {
        return hashMap;
    }

    public void setHashMap(Map<String, String> hashMap) {
        this.hashMap = hashMap;
    }

    public String getOnlyClassName() {
        if (this.hashMap == null || this.hashMap.isEmpty()) {
            throw new RuntimeException("HASH MAP IS NULL");
        }
        for (Map.Entry<String, String> entry : this.hashMap.entrySet()) {
            return entry.getKey();
        }
        throw new RuntimeException("HASH MAP UNKNOWN ERROR");
    }

    public String getOnlyHash() {
        if (this.hashMap == null || this.hashMap.isEmpty()) {
            throw new RuntimeException("HASH MAP IS NULL");
        }
        for (Map.Entry<String, String> entry : this.hashMap.entrySet()) {
            return entry.getValue();
        }
        throw new RuntimeException("HASH MAP UNKNOWN ERROR");
    }
}
