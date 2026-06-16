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

import java.util.Collections;
import java.util.List;

/**
 * DAG 节点抽象。
 * <p>
 * 每个节点接收 0..N 个上游输入（按 {@link DagEdge#getToPort()} 的索引），输出一个 {@link NodeResult}。
 * <p>
 * 实现类必须是无状态或在 execute 内自行同步——{@link DagExecutor} 在拓扑顺序内串行调度普通节点，
 * 仅 {@link me.n1ar4.jar.analyzer.ai.workflow.nodes.LoopOverItemsNode} 等特殊节点会内部并发。
 */
public abstract class DagNode {

    private final String id;
    private final String name;

    /**
     * 仅作可视化展示的节点：DagExecutor 不会真正调用 {@link #execute}，
     * 由外部（例如内联在 LoopOverItemsNode 里的 ItemHandler）通过
     * {@code ctx.emit(...)} 与 {@code ctx.putOutput(...)} 控制其状态。
     * <p>
     * 用于在画布上画出 "loop 内部子流程" 的几个节点，让用户看到完整 DAG。
     */
    private boolean displayOnly;

    protected DagNode(String id, String name) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("node id required");
        }
        this.id = id;
        this.name = name == null ? id : name;
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public boolean isDisplayOnly() {
        return displayOnly;
    }

    public DagNode setDisplayOnly(boolean displayOnly) {
        this.displayOnly = displayOnly;
        return this;
    }

    /**
     * 节点执行入口。
     *
     * @param ctx    上下文
     * @param inputs 已按端口索引排序好的上游输出（缺失的用 NodeResult.skipped() 占位）
     */
    public abstract NodeResult execute(DagContext ctx, List<NodeResult> inputs) throws Exception;

    /**
     * 期望的输入端口数；返回 0 表示无输入（trigger 节点）。
     */
    public int requiredInputs() {
        return 1;
    }

    @SuppressWarnings("unchecked")
    protected static <T> List<T> asList(Object o) {
        if (o == null) {
            return Collections.emptyList();
        }
        if (o instanceof List) {
            return (List<T>) o;
        }
        throw new IllegalArgumentException("expected list, got " + o.getClass().getName());
    }
}
