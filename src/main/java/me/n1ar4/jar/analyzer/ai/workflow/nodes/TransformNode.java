/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow.nodes;

import me.n1ar4.jar.analyzer.ai.workflow.core.DagContext;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagNode;
import me.n1ar4.jar.analyzer.ai.workflow.core.NodeResult;

import java.util.List;
import java.util.function.BiFunction;

/**
 * 转换节点：把上游输出按用户给定的 Java Lambda 进行转换，对应 n8n 的 Code (JS) 节点。
 * <p>
 * 不使用任何脚本引擎（避免 RCE 风险）：转换函数由 Java 代码直接提供。
 */
public final class TransformNode extends DagNode {

    private final BiFunction<DagContext, Object, Object> transformer;

    public TransformNode(String id, String name,
                         BiFunction<DagContext, Object, Object> transformer) {
        super(id, name == null ? "Transform" : name);
        if (transformer == null) {
            throw new IllegalArgumentException("transformer required");
        }
        this.transformer = transformer;
    }

    @Override
    public NodeResult execute(DagContext ctx, List<NodeResult> inputs) {
        Object data = (inputs == null || inputs.isEmpty()) ? null : inputs.get(0).getData();
        Object out = transformer.apply(ctx, data);
        return NodeResult.ok(out);
    }
}
