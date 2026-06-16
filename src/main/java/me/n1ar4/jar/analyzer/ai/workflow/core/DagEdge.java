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

/**
 * DAG 边：from 节点的某个分支 -> to 节点的某个输入端口（index）。
 */
public final class DagEdge {
    private final String from;
    private final String fromBranch;
    private final String to;
    private final int toPort;

    public DagEdge(String from, String fromBranch, String to, int toPort) {
        this.from = from;
        this.fromBranch = fromBranch == null ? NodeResult.DEFAULT_BRANCH : fromBranch;
        this.to = to;
        this.toPort = toPort;
    }

    public DagEdge(String from, String to) {
        this(from, NodeResult.DEFAULT_BRANCH, to, 0);
    }

    public String getFrom() {
        return from;
    }

    public String getFromBranch() {
        return fromBranch;
    }

    public String getTo() {
        return to;
    }

    public int getToPort() {
        return toPort;
    }

    @Override
    public String toString() {
        return from + ":" + fromBranch + " -> " + to + "[" + toPort + "]";
    }
}
