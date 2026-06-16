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

import me.n1ar4.jar.analyzer.ai.AIConfig;
import me.n1ar4.jar.analyzer.ai.workflow.gui.NodeKind;
import me.n1ar4.jar.analyzer.ai.workflow.gui.NodeView;
import me.n1ar4.jar.analyzer.ai.workflow.gui.WorkflowGraphModel;
import me.n1ar4.jar.analyzer.ai.workflow.presets.JarAnalyzerSecurityWorkflow;
import me.n1ar4.jar.analyzer.ai.workflow.report.ReportStore;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 画布模型构建（不打开 GUI），保证视图层与 DAG 引擎能正确联动。
 */
class WorkflowGraphModelTest {

    @Test
    void canBuildViewModelFromPresetWorkflow() {
        AIConfig cfg = new AIConfig();
        cfg.setApiKey("dummy");
        cfg.setBaseUrl("http://127.0.0.1:1");
        cfg.setModel("test-model");

        JarAnalyzerSecurityWorkflow wf = new JarAnalyzerSecurityWorkflow(
                cfg, "http://127.0.0.1:10032", new ReportStore(), 50, 5);

        WorkflowGraphModel model = WorkflowGraphModel.from(wf.buildGraph());
        assertNotNull(model);
        // 预置 workflow 至少包含 13 个节点（constants/4 http/merge/loop + 6 inner）
        assertTrue(model.getNodes().size() >= 13,
                "expected >=13 nodes, got " + model.getNodes().size());
        assertFalse(model.getEdges().isEmpty());

        // 节点 ID 唯一
        Set<String> ids = new HashSet<>();
        for (NodeView nv : model.getNodes()) {
            assertTrue(ids.add(nv.getId()), "duplicate id: " + nv.getId());
            assertNotNull(nv.getKind(), "node kind null: " + nv.getId());
        }

        // 各 kind 都能拿到 SVG 图标（确保资源未丢失）
        for (NodeKind k : NodeKind.values()) {
            assertNotNull(k.icon(), "missing icon for: " + k);
        }

        // 总尺寸应大于 0
        assertTrue(model.totalWidth() > 0);
        assertTrue(model.totalHeight() > 0);

        // find() 索引可用
        assertNotNull(model.find("constants"));
        assertNotNull(model.find("merge"));
        assertNotNull(model.find("loop"));
    }

    @Test
    void layoutAssignsDistinctXPerLayer() {
        AIConfig cfg = new AIConfig();
        cfg.setApiKey("dummy");
        cfg.setBaseUrl("http://127.0.0.1:1");
        cfg.setModel("test-model");

        WorkflowGraphModel model = WorkflowGraphModel.from(
                new JarAnalyzerSecurityWorkflow(
                        cfg, "http://127.0.0.1:10032", new ReportStore(), 1, 1)
                        .buildGraph());

        // constants 应在最左侧（x 最小），loop 应在最右侧（x 最大）
        NodeView c = model.find("constants");
        NodeView m = model.find("merge");
        NodeView l = model.find("loop");
        assertNotNull(c);
        assertNotNull(m);
        assertNotNull(l);
        assertTrue(c.getX() < m.getX(), "constants should be left of merge");
        assertTrue(m.getX() < l.getX(), "merge should be left of loop");

        // 每层节点数量正确（去重每个 x 的节点 id 数）
        HashMap<Double, Integer> count = new HashMap<>();
        for (NodeView nv : model.getNodes()) {
            count.merge(nv.getX(), 1, Integer::sum);
        }
        // 4 个 HTTP 节点应在同一层（同一 x）
        boolean foundHttpLayer = false;
        for (Integer v : count.values()) {
            if (v == 4) {
                foundHttpLayer = true;
                break;
            }
        }
        assertTrue(foundHttpLayer, "expected one layer with 4 http nodes");
        // x 列数等于层数。当前预置 workflow 有 10 列：
        // constants / 4-http(同列) / merge / loop / getMethodsInner /
        // ifMethods / getClassInner / prepPrompt / aiAgent / reportSink
        assertEquals(10, count.size());
    }

    @Test
    void innerSubgraphPresent() {
        AIConfig cfg = new AIConfig();
        cfg.setApiKey("dummy");
        cfg.setBaseUrl("http://127.0.0.1:1");
        cfg.setModel("test-model");

        WorkflowGraphModel model = WorkflowGraphModel.from(
                new JarAnalyzerSecurityWorkflow(
                        cfg, "http://127.0.0.1:10032", new ReportStore(), 1, 1)
                        .buildGraph());

        // loop 内部 6 个展示节点必须都存在
        for (String id : new String[]{
                "getMethodsInner", "ifMethods", "getClassInner",
                "prepPrompt", "aiAgent", "reportSink"}) {
            assertNotNull(model.find(id), "missing inner node: " + id);
        }
        // 它们应一字排开，X 单调递增
        double xPrev = -1;
        for (String id : new String[]{
                "loop", "getMethodsInner", "ifMethods", "getClassInner",
                "prepPrompt", "aiAgent", "reportSink"}) {
            NodeView nv = model.find(id);
            assertNotNull(nv);
            assertTrue(nv.getX() > xPrev,
                    "x not increasing at " + id + " (" + nv.getX() + ")");
            xPrev = nv.getX();
        }
    }

    @Test
    void compactLayoutFoldsInnerToThreeRows() {
        AIConfig cfg = new AIConfig();
        cfg.setApiKey("dummy");
        cfg.setBaseUrl("http://127.0.0.1:1");
        cfg.setModel("test-model");

        WorkflowGraphModel model = WorkflowGraphModel.from(
                new JarAnalyzerSecurityWorkflow(
                        cfg, "http://127.0.0.1:10032", new ReportStore(), 1, 1)
                        .buildGraph());
        JarAnalyzerSecurityWorkflow.applyCompactLayout(model);

        NodeView merge = model.find("merge");
        NodeView loop = model.find("loop");
        NodeView gm = model.find("getMethodsInner");
        NodeView ifn = model.find("ifMethods");
        NodeView gc = model.find("getClassInner");
        NodeView pp = model.find("prepPrompt");
        NodeView ai = model.find("aiAgent");
        NodeView rs = model.find("reportSink");

        // 同列对齐
        assertEquals(merge.getX(), gm.getX(), 0.5);
        assertEquals(merge.getX(), gc.getX(), 0.5);
        assertEquals(merge.getX(), ai.getX(), 0.5);
        assertEquals(loop.getX(), ifn.getX(), 0.5);
        assertEquals(loop.getX(), pp.getX(), 0.5);
        assertEquals(loop.getX(), rs.getX(), 0.5);

        // 三行 Y 严格递增
        assertTrue(gm.getY() > merge.getY());
        assertTrue(gc.getY() > gm.getY());
        assertTrue(ai.getY() > gc.getY());
        // 同一行的两个节点 Y 相等
        assertEquals(gm.getY(), ifn.getY(), 0.5);
        assertEquals(gc.getY(), pp.getY(), 0.5);
        assertEquals(ai.getY(), rs.getY(), 0.5);
    }
}
