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

import me.n1ar4.jar.analyzer.ai.workflow.core.DagContext;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagExecutor;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagGraph;
import me.n1ar4.jar.analyzer.ai.workflow.gui.LoopHistory;
import me.n1ar4.jar.analyzer.ai.workflow.nodes.LoopOverItemsNode;
import me.n1ar4.jar.analyzer.ai.workflow.nodes.TransformNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 LoopOverItemsNode 在每条 item 处理时都会发出 LoopEvent，
 * 并且 LoopHistory 能正确按迭代分组。
 */
class LoopHistoryTest {

    @Test
    void capturesEachIterationLifecycle() {
        DagGraph g = new DagGraph();
        g.addNode(new TransformNode("src", "src", (ctx, in) -> {
            List<Object> items = new ArrayList<>();
            items.add(map("className", "com.A"));
            items.add(map("className", "com.B"));
            items.add(map("className", "com.C"));
            return items;
        }));
        g.addNode(new LoopOverItemsNode<Map<String, Object>>("loop",
                (ctx, item, idx, total) -> {
                    // 子步骤手动发一条 STEP，模拟工作流里的多步处理
                    ctx.emitLoop(new DagContext.LoopEvent("loop", idx, total,
                            DagContext.LoopEvent.Phase.STEP,
                            String.valueOf(item.get("className")),
                            "doing real work"));
                    return item.get("className");
                }, true, 0));
        g.addEdge("src", "loop");

        DagContext ctx = new DagContext();
        LoopHistory hist = new LoopHistory();
        ctx.setLoopListener(hist);
        new DagExecutor(g).run(ctx);

        // 三个迭代都应被记录
        assertEquals(3, hist.totalIterations("loop"));
        assertEquals(3, hist.iterationsOf("loop").size());

        // 每个迭代至少有 STEP（自身发的 step + 用户 handler 发的 step + ITEM_DONE）
        for (int i = 0; i < 3; i++) {
            List<DagContext.LoopEvent> evs = hist.get("loop", i);
            assertTrue(evs.size() >= 2, "iter " + i + " events=" + evs.size());
            // 必须有 ITEM_DONE 终态
            boolean hasDone = false;
            for (DagContext.LoopEvent e : evs) {
                if (e.getPhase() == DagContext.LoopEvent.Phase.ITEM_DONE) {
                    hasDone = true;
                    break;
                }
            }
            assertTrue(hasDone, "iter " + i + " missing ITEM_DONE");
        }
        // 全局 START 事件
        boolean hasStart = false;
        for (DagContext.LoopEvent e : hist.snapshotAll()) {
            if (e.getPhase() == DagContext.LoopEvent.Phase.START) {
                hasStart = true;
                break;
            }
        }
        assertTrue(hasStart);
    }

    private static Map<String, Object> map(String k, Object v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k, v);
        return m;
    }
}
