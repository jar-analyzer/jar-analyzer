/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.gui.vul;

import me.n1ar4.jar.analyzer.engine.SearchCondition;

import java.util.List;
import java.util.Map;

public class Rule {
    private String name;
    private Map<String, List<SearchCondition>> vulnerabilities;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, List<SearchCondition>> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(Map<String, List<SearchCondition>> vulnerabilities) {
        this.vulnerabilities = vulnerabilities;
    }
}
