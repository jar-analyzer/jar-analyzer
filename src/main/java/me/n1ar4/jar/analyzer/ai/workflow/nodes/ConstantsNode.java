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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局常量节点，对应 n8n 的 Global Constants。
 * <p>
 * 节点输出常量 Map，本身也写入 ctx.constants 供后续节点通过 {@link DagContext#constant(String)} 访问。
 */
public final class ConstantsNode extends DagNode {

    private final Map<String, String> constants;

    public ConstantsNode(String id, Map<String, String> constants) {
        super(id, "Global Constants");
        this.constants = constants == null ? new HashMap<String, String>() : new HashMap<>(constants);
    }

    @Override
    public int requiredInputs() {
        return 0;
    }

    @Override
    public NodeResult execute(DagContext ctx, List<NodeResult> inputs) {
        for (Map.Entry<String, String> e : constants.entrySet()) {
            ctx.putConstant(e.getKey(), e.getValue());
        }
        return NodeResult.ok(new HashMap<>(constants));
    }
}
