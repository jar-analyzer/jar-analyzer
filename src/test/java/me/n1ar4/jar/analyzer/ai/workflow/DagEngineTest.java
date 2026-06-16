/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.ai.workflow;

import me.n1ar4.jar.analyzer.ai.workflow.core.*;
import me.n1ar4.jar.analyzer.ai.workflow.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 不依赖任何外部服务（HTTP / LLM）的 DAG 引擎单元测试。
 */
class DagEngineTest {

    @Test
    void constantsAndTransformPipeline() {
        DagGraph g = new DagGraph();
        Map<String, String> consts = new HashMap<>();
        consts.put("k1", "v1");
        g.addNode(new ConstantsNode("c", consts));
        g.addNode(new TransformNode("t", "echo", (ctx, in) -> {
            // ConstantsNode 输出常量 Map
            assertNotNull(in);
            return ctx.constant("k1");
        }));
        g.addEdge("c", "t");

        Map<String, NodeResult> out = new DagExecutor(g).run(new DagContext());
        assertEquals(NodeStatus.SUCCESS, out.get("t").getStatus());
        assertEquals("v1", out.get("t").getData());
    }

    @Test
    void mergeFanIn() {
        DagGraph g = new DagGraph();
        g.addNode(new TransformNode("a", "a",
                (ctx, in) -> Arrays.asList(map("k", "a1"), map("k", "a2"))));
        g.addNode(new TransformNode("b", "b",
                (ctx, in) -> Arrays.asList(map("k", "b1"))));
        g.addNode(new TransformNode("c", "c",
                (ctx, in) -> Arrays.asList(map("k", "c1"), map("k", "c2"), map("k", "c3"))));
        g.addNode(new MergeNode("m", 3));
        // 三个 trigger 的 noop 入口
        g.addNode(new TransformNode("seed", "seed", (ctx, in) -> "go"));
        g.addEdge("seed", "a");
        g.addEdge("seed", "b");
        g.addEdge("seed", "c");
        g.addEdge("a", null, "m", 0);
        g.addEdge("b", null, "m", 1);
        g.addEdge("c", null, "m", 2);

        Map<String, NodeResult> out = new DagExecutor(g).run(new DagContext());
        Object data = out.get("m").getData();
        assertTrue(data instanceof List);
        assertEquals(6, ((List<?>) data).size());
    }

    @Test
    void ifBranches() {
        DagGraph g = new DagGraph();
        g.addNode(new TransformNode("src", "src", (ctx, in) -> "hello"));
        g.addNode(new IfNode("if", IfNode.notEmpty()));
        g.addNode(new TransformNode("trueBranch", "true",
                (ctx, in) -> "TRUE:" + in));
        g.addNode(new TransformNode("falseBranch", "false",
                (ctx, in) -> "FALSE:" + in));

        g.addEdge("src", "if");
        g.addEdge("if", IfNode.TRUE_BRANCH, "trueBranch", 0);
        g.addEdge("if", IfNode.FALSE_BRANCH, "falseBranch", 0);

        Map<String, NodeResult> out = new DagExecutor(g).run(new DagContext());
        assertEquals(NodeStatus.SUCCESS, out.get("trueBranch").getStatus());
        assertEquals("TRUE:hello", out.get("trueBranch").getData());
        // false branch 因为 from-branch 不匹配 -> 上游可见输入全为 SKIPPED -> 跳过
        assertEquals(NodeStatus.SKIPPED, out.get("falseBranch").getStatus());
    }

    @Test
    void loopHandlerCallsPerItem() {
        DagGraph g = new DagGraph();
        g.addNode(new TransformNode("src", "src", (ctx, in) -> {
            List<Object> items = new ArrayList<>();
            items.add(map("name", "x"));
            items.add(map("name", "y"));
            items.add(map("name", "z"));
            return items;
        }));
        g.addNode(new LoopOverItemsNode<Map<String, Object>>("loop",
                (ctx, item, idx, total) -> "item-" + item.get("name") + "-" + idx,
                true, 0));
        g.addEdge("src", "loop");

        Map<String, NodeResult> out = new DagExecutor(g).run(new DagContext());
        Object loopOut = out.get("loop").getData();
        assertTrue(loopOut instanceof List);
        List<?> list = (List<?>) loopOut;
        assertEquals(3, list.size());
        assertEquals("item-x-0", list.get(0));
    }

    @Test
    void cycleDetected() {
        DagGraph g = new DagGraph();
        g.addNode(new TransformNode("a", "a", (ctx, in) -> in));
        g.addNode(new TransformNode("b", "b", (ctx, in) -> in));
        g.addEdge("a", "b");
        g.addEdge("b", "a");
        try {
            g.topoSort();
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("cycle"));
            return;
        }
        throw new AssertionError("cycle not detected");
    }

    private static Map<String, Object> map(String k, Object v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k, v);
        return m;
    }
}
