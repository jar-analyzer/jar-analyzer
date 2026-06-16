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
 * 节点执行结果。
 * <p>
 * data 是节点的主输出（JSON 友好对象，常见类型：{@code List<Map>}、{@code Map}、{@code String}）。
 * 对于多分支节点（如 IfNode），可通过 {@link #getBranch()} 指定后续命中的分支端口名。
 */
public final class NodeResult {

    public static final String DEFAULT_BRANCH = "main";

    private final NodeStatus status;
    private final Object data;
    private final String branch;
    private final Throwable error;

    private NodeResult(NodeStatus status, Object data, String branch, Throwable error) {
        this.status = status;
        this.data = data;
        this.branch = branch == null ? DEFAULT_BRANCH : branch;
        this.error = error;
    }

    public static NodeResult ok(Object data) {
        return new NodeResult(NodeStatus.SUCCESS, data, DEFAULT_BRANCH, null);
    }

    public static NodeResult ok(Object data, String branch) {
        return new NodeResult(NodeStatus.SUCCESS, data, branch, null);
    }

    public static NodeResult skipped() {
        return new NodeResult(NodeStatus.SKIPPED, null, DEFAULT_BRANCH, null);
    }

    public static NodeResult failed(Throwable t) {
        return new NodeResult(NodeStatus.FAILED, null, DEFAULT_BRANCH, t);
    }

    public NodeStatus getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    public String getBranch() {
        return branch;
    }

    public Throwable getError() {
        return error;
    }
}
