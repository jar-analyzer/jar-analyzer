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
import java.util.function.Predicate;

/**
 * If 节点：根据上游输出选择 main / fallback 分支。
 * <p>
 * 命中 predicate 时输出走 branch=true，否则 branch=false。下游边在 from-branch 上做匹配。
 */
public final class IfNode extends DagNode {

    public static final String TRUE_BRANCH = "true";
    public static final String FALSE_BRANCH = "false";

    private final Predicate<Object> predicate;

    public IfNode(String id, Predicate<Object> predicate) {
        super(id, "If");
        this.predicate = predicate == null ? notEmpty() : predicate;
    }

    @Override
    public NodeResult execute(DagContext ctx, List<NodeResult> inputs) {
        Object data = (inputs == null || inputs.isEmpty()) ? null : inputs.get(0).getData();
        boolean matched;
        try {
            matched = predicate.test(data);
        } catch (Throwable t) {
            matched = false;
        }
        return NodeResult.ok(data, matched ? TRUE_BRANCH : FALSE_BRANCH);
    }

    public static Predicate<Object> notEmpty() {
        return new Predicate<Object>() {
            @Override
            public boolean test(Object o) {
                if (o == null) {
                    return false;
                }
                if (o instanceof CharSequence) {
                    return ((CharSequence) o).length() > 0;
                }
                if (o instanceof java.util.Collection) {
                    return !((java.util.Collection<?>) o).isEmpty();
                }
                if (o instanceof java.util.Map) {
                    return !((java.util.Map<?, ?>) o).isEmpty();
                }
                return true;
            }
        };
    }
}
