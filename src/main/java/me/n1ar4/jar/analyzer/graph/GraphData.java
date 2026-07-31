/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.graph;

import java.util.List;
import java.util.Map;

public class GraphData {
    private String currentNodeId;
    private List<Map<String, String>> nodes;
    private List<Map<String, String>> links;

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public List<Map<String, String>> getNodes() {
        return nodes;
    }

    public void setNodes(List<Map<String, String>> nodes) {
        this.nodes = nodes;
    }

    public List<Map<String, String>> getLinks() {
        return links;
    }

    public void setLinks(List<Map<String, String>> links) {
        this.links = links;
    }
}
