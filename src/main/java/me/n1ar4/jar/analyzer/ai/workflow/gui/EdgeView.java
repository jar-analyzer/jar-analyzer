/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.gui;

/**
 * 边视图：源节点 / 目标节点 / 端口 / 命中分支 / 高亮态。
 */
public final class EdgeView {

    private final String fromId;
    private final String fromBranch;
    private final String toId;
    private final int toPort;

    /**
     * 当 from 节点最终输出 branch 与 fromBranch 一致时为 true，画线时着重高亮。
     */
    private boolean active;

    public EdgeView(String fromId, String fromBranch, String toId, int toPort) {
        this.fromId = fromId;
        this.fromBranch = fromBranch == null ? "main" : fromBranch;
        this.toId = toId;
        this.toPort = toPort;
    }

    public String getFromId() {
        return fromId;
    }

    public String getFromBranch() {
        return fromBranch;
    }

    public String getToId() {
        return toId;
    }

    public int getToPort() {
        return toPort;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
