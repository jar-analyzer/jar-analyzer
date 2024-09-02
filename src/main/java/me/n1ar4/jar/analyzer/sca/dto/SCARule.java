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
