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

import java.util.ArrayList;
import java.util.List;

/**
 * Merge 节点：把 N 个输入端口的 List 拍平为一个 List 输出。
 * 对应 n8n 的 Merge (mode=append) 节点。
 */
public final class MergeNode extends DagNode {

    private final int inputCount;

    public MergeNode(String id, int inputCount) {
        super(id, "Merge");
        this.inputCount = Math.max(1, inputCount);
    }

    @Override
    public int requiredInputs() {
        return inputCount;
    }

    @Override
    public NodeResult execute(DagContext ctx, List<NodeResult> inputs) {
        List<Object> merged = new ArrayList<>();
        if (inputs != null) {
            for (NodeResult r : inputs) {
                if (r == null || r.getData() == null) {
                    continue;
                }
                Object d = r.getData();
                if (d instanceof List) {
                    for (Object o : (List<?>) d) {
                        if (o != null) {
                            merged.add(o);
                        }
                    }
                } else {
                    merged.add(d);
                }
            }
        }
        return NodeResult.ok(merged);
    }
}
