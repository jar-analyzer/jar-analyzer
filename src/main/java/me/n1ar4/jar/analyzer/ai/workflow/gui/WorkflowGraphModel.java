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

import me.n1ar4.jar.analyzer.ai.workflow.core.DagEdge;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagGraph;
import me.n1ar4.jar.analyzer.ai.workflow.core.DagNode;

import java.util.*;

/**
 * 把 {@link DagGraph} 转换为画布可渲染的 {@code List<NodeView> + List<EdgeView>}，
 * 并使用一个简单的分层布局算法（Sugiyama 简化版）：
 * <ol>
 *   <li>按拓扑序为每个节点分配 <em>层</em>（layer = max(parent.layer)+1）</li>
 *   <li>同一层内按入图先后顺序排列在 Y 方向</li>
 *   <li>X 方向 = layer * (NODE_WIDTH + H_GAP)</li>
 * </ol>
 */
public final class WorkflowGraphModel {

    /**
     * 同一层节点的水平间隔（像素）。
     */
    public static final int H_GAP = 110;
    /**
     * 同一层不同节点之间的垂直间隔。
     */
    public static final int V_GAP = 30;
    /**
     * 整体上左 padding。
     */
    public static final int PADDING_X = 60;
    public static final int PADDING_Y = 60;

    private final List<NodeView> nodes;
    private final List<EdgeView> edges;
    private final Map<String, NodeView> idIndex;

    private WorkflowGraphModel(List<NodeView> nodes, List<EdgeView> edges) {
        this.nodes = Collections.unmodifiableList(nodes);
        this.edges = Collections.unmodifiableList(edges);
        Map<String, NodeView> idx = new HashMap<>();
        for (NodeView nv : nodes) {
            idx.put(nv.getId(), nv);
        }
        this.idIndex = Collections.unmodifiableMap(idx);
    }

    public List<NodeView> getNodes() {
        return nodes;
    }

    public List<EdgeView> getEdges() {
        return edges;
    }

    public NodeView find(String id) {
        return idIndex.get(id);
    }

    /**
     * 画布需要的总宽高（像素）。
     */
    public int totalWidth() {
        double max = 0;
        for (NodeView v : nodes) {
            max = Math.max(max, v.getX() + NodeView.WIDTH);
        }
        return (int) (max + PADDING_X);
    }

    public int totalHeight() {
        double max = 0;
        for (NodeView v : nodes) {
            max = Math.max(max, v.getY() + NodeView.HEIGHT);
        }
        return (int) (max + PADDING_Y);
    }

    /**
     * 从 DagGraph 构建 GUI 模型，并完成布局。
     */
    public static WorkflowGraphModel from(DagGraph graph) {
        if (graph == null) {
            return new WorkflowGraphModel(
                    new ArrayList<NodeView>(), new ArrayList<EdgeView>());
        }
        // 1) 拓扑排序得到执行顺序
        List<String> order = graph.topoSort();
        // 2) 计算每个节点的 layer
        Map<String, Integer> layer = new HashMap<>();
        for (String id : order) {
            int my = 0;
            for (DagEdge e : graph.incomingEdges(id)) {
                Integer pl = layer.get(e.getFrom());
                if (pl != null && pl + 1 > my) {
                    my = pl + 1;
                }
            }
            layer.put(id, my);
        }
        // 3) 每层桶
        Map<Integer, List<String>> buckets = new HashMap<>();
        int maxLayer = 0;
        for (Map.Entry<String, Integer> en : layer.entrySet()) {
            int l = en.getValue();
            maxLayer = Math.max(maxLayer, l);
            List<String> b = buckets.get(l);
            if (b == null) {
                b = new ArrayList<>();
                buckets.put(l, b);
            }
            b.add(en.getKey());
        }
        // 4) 给每个节点分配坐标
        List<NodeView> views = new ArrayList<>();
        Map<String, NodeView> idx = new HashMap<>();
        for (int l = 0; l <= maxLayer; l++) {
            List<String> ids = buckets.get(l);
            if (ids == null) {
                continue;
            }
            // 这一列的总高度
            int colHeight = ids.size() * NodeView.HEIGHT + Math.max(0, ids.size() - 1) * V_GAP;
            int colTop = PADDING_Y;  // 顶部对齐，简单稳定
            int x = PADDING_X + l * (NodeView.WIDTH + H_GAP);
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                DagNode dn = graph.getNode(id);
                NodeKind kind = NodeKind.of(dn);
                int y = colTop + i * (NodeView.HEIGHT + V_GAP);
                NodeView nv = new NodeView(id, dn.getName(), kind, x, y);
                views.add(nv);
                idx.put(id, nv);
            }
            // 兼容性：未使用，仅占位防止 IDE 警告
            if (colHeight < 0) {
                throw new IllegalStateException("unreachable");
            }
        }
        // 5) 构建边
        List<EdgeView> edges = new ArrayList<>();
        for (DagEdge e : graph.getEdges()) {
            edges.add(new EdgeView(e.getFrom(), e.getFromBranch(), e.getTo(), e.getToPort()));
        }
        return new WorkflowGraphModel(views, edges);
    }
}
