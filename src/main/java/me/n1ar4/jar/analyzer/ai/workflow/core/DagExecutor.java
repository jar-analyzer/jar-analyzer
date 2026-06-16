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

import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.*;

/**
 * 拓扑顺序执行 DAG。
 * <p>
 * 简化模型：
 * - 节点按拓扑顺序串行执行（与 n8n 默认 v1 执行顺序一致）
 * - 节点失败默认抛错，可在节点内部 try/catch 转为 {@link NodeResult#skipped()} 实现 onError=continue
 * - 分支：节点结果的 {@link NodeResult#getBranch()} 决定下游边是否激活；不匹配的边 -> 下游节点本端口收到 SKIPPED
 */
public final class DagExecutor {

    private static final Logger logger = LogManager.getLogger();

    private final DagGraph graph;

    public DagExecutor(DagGraph graph) {
        this.graph = graph;
    }

    public Map<String, NodeResult> run(DagContext ctx) {
        if (ctx == null) {
            ctx = new DagContext();
        }
        List<String> order = graph.topoSort();
        // 已激活的边：from-branch 与 fromNode 的实际 branch 匹配
        Set<String> activeEdgeKeys = new HashSet<>();

        for (String nodeId : order) {
            if (ctx.isCancelled()) {
                ctx.putOutput(nodeId, NodeResult.skipped());
                ctx.emit(nodeId, NodeStatus.CANCELLED, "user cancelled");
                continue;
            }
            DagNode node = graph.getNode(nodeId);
            // displayOnly 节点不被执行器驱动，由外部（如 LoopOverItems 的 ItemHandler）控制状态
            if (node.isDisplayOnly()) {
                continue;
            }
            // 收集入边对应的输入；按 toPort 排序，未激活/上游未运行的端口塞 skipped
            List<DagEdge> in = graph.incomingEdges(nodeId);
            // 没有入边的节点视为 trigger（直接运行，输入列表为空）
            if (in.isEmpty()) {
                runOne(ctx, node, new ArrayList<NodeResult>(), activeEdgeKeys);
                continue;
            }
            // 端口聚合时，displayOnly 上游被视为 SUCCESS 占位
            // 端口聚合：同一 toPort 可能有多条入边
            Map<Integer, List<NodeResult>> portMap = new HashMap<>();
            int maxPort = 0;
            for (DagEdge e : in) {
                int p = e.getToPort();
                if (p > maxPort) {
                    maxPort = p;
                }
                NodeResult fromOut = ctx.getOutput(e.getFrom());
                DagNode fromNode = graph.getNode(e.getFrom());
                NodeResult portIn;
                if (fromNode != null && fromNode.isDisplayOnly()) {
                    // displayOnly 上游：当前节点同样作为 displayOnly 处理（不再继续推断输入）
                    portIn = NodeResult.skipped();
                } else if (fromOut == null || fromOut.getStatus() != NodeStatus.SUCCESS) {
                    portIn = NodeResult.skipped();
                } else if (!fromOut.getBranch().equals(e.getFromBranch())) {
                    // 上游节点选择了别的分支
                    portIn = NodeResult.skipped();
                } else {
                    activeEdgeKeys.add(edgeKey(e));
                    portIn = fromOut;
                }
                portMap.computeIfAbsent(p, k -> new ArrayList<NodeResult>()).add(portIn);
            }
            int needed = Math.max(node.requiredInputs(), maxPort + 1);
            List<NodeResult> inputs = new ArrayList<>(needed);
            for (int i = 0; i < needed; i++) {
                List<NodeResult> ps = portMap.get(i);
                if (ps == null || ps.isEmpty()) {
                    inputs.add(NodeResult.skipped());
                } else if (ps.size() == 1) {
                    inputs.add(ps.get(0));
                } else {
                    // 同一端口多条入边：取第一个 SUCCESS 的
                    NodeResult chosen = null;
                    for (NodeResult r : ps) {
                        if (r != null && r.getStatus() == NodeStatus.SUCCESS) {
                            chosen = r;
                            break;
                        }
                    }
                    inputs.add(chosen != null ? chosen : NodeResult.skipped());
                }
            }
            // 全部端口都是 SKIPPED -> 节点跳过
            boolean anyInput = false;
            for (NodeResult r : inputs) {
                if (r != null && r.getStatus() == NodeStatus.SUCCESS) {
                    anyInput = true;
                    break;
                }
            }
            if (!anyInput && node.requiredInputs() > 0) {
                ctx.putOutput(nodeId, NodeResult.skipped());
                ctx.emit(nodeId, NodeStatus.SKIPPED, "no active input");
                continue;
            }
            runOne(ctx, node, inputs, activeEdgeKeys);
        }
        return new HashMap<>(ctx.getNodeOutputs());
    }

    private void runOne(DagContext ctx, DagNode node, List<NodeResult> inputs,
                        Set<String> activeEdgeKeys) {
        ctx.emit(node.getId(), NodeStatus.RUNNING, node.getName());
        try {
            NodeResult r = node.execute(ctx, inputs);
            if (r == null) {
                r = NodeResult.skipped();
            }
            ctx.putOutput(node.getId(), r);
            ctx.emit(node.getId(), r.getStatus(), node.getName());
        } catch (Throwable t) {
            logger.error("node {} ({}) failed: {}", node.getId(), node.getName(), t.toString());
            ctx.putOutput(node.getId(), NodeResult.failed(t));
            ctx.emit(node.getId(), NodeStatus.FAILED, t.getMessage());
        }
    }

    private static String edgeKey(DagEdge e) {
        return e.getFrom() + "/" + e.getFromBranch() + "->" + e.getTo() + "[" + e.getToPort() + "]";
    }
}
