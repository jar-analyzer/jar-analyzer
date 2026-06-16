/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.core;

import java.util.*;

/**
 * DAG 容器。负责节点和边的存储以及拓扑排序。
 */
public final class DagGraph {

    private final Map<String, DagNode> nodes = new LinkedHashMap<>();
    private final List<DagEdge> edges = new ArrayList<>();

    public DagGraph addNode(DagNode node) {
        if (nodes.containsKey(node.getId())) {
            throw new IllegalArgumentException("duplicate node id: " + node.getId());
        }
        nodes.put(node.getId(), node);
        return this;
    }

    public DagGraph addEdge(DagEdge edge) {
        if (!nodes.containsKey(edge.getFrom())) {
            throw new IllegalArgumentException("unknown from node: " + edge.getFrom());
        }
        if (!nodes.containsKey(edge.getTo())) {
            throw new IllegalArgumentException("unknown to node: " + edge.getTo());
        }
        edges.add(edge);
        return this;
    }

    public DagGraph addEdge(String from, String to) {
        return addEdge(new DagEdge(from, to));
    }

    public DagGraph addEdge(String from, String fromBranch, String to, int toPort) {
        return addEdge(new DagEdge(from, fromBranch, to, toPort));
    }

    public DagNode getNode(String id) {
        return nodes.get(id);
    }

    public Map<String, DagNode> getNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public List<DagEdge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * 入边（按 toPort 排序）。
     */
    public List<DagEdge> incomingEdges(String nodeId) {
        List<DagEdge> in = new ArrayList<>();
        for (DagEdge e : edges) {
            if (e.getTo().equals(nodeId)) {
                in.add(e);
            }
        }
        in.sort((a, b) -> Integer.compare(a.getToPort(), b.getToPort()));
        return in;
    }

    public List<DagEdge> outgoingEdges(String nodeId) {
        List<DagEdge> out = new ArrayList<>();
        for (DagEdge e : edges) {
            if (e.getFrom().equals(nodeId)) {
                out.add(e);
            }
        }
        return out;
    }

    /**
     * Kahn 拓扑排序，发现环时抛错。
     */
    public List<String> topoSort() {
        Map<String, Integer> indeg = new HashMap<>();
        for (String id : nodes.keySet()) {
            indeg.put(id, 0);
        }
        for (DagEdge e : edges) {
            indeg.merge(e.getTo(), 1, Integer::sum);
        }
        ArrayList<String> queue = new ArrayList<>();
        for (Map.Entry<String, Integer> en : indeg.entrySet()) {
            if (en.getValue() == 0) {
                queue.add(en.getKey());
            }
        }
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        while (!queue.isEmpty()) {
            String cur = queue.remove(0);
            if (!visited.add(cur)) {
                continue;
            }
            order.add(cur);
            for (DagEdge e : outgoingEdges(cur)) {
                Integer n = indeg.get(e.getTo());
                if (n == null) {
                    continue;
                }
                int nv = n - 1;
                indeg.put(e.getTo(), nv);
                if (nv == 0) {
                    queue.add(e.getTo());
                }
            }
        }
        if (order.size() != nodes.size()) {
            throw new IllegalStateException("graph has cycle, sorted="
                    + order.size() + " total=" + nodes.size());
        }
        return order;
    }
}
