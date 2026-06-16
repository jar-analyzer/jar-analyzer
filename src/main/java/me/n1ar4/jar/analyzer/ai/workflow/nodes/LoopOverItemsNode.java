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
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 对 List 输入做迭代，针对每个 item 调用 {@link ItemHandler}。
 * <p>
 * 与 n8n 的 splitInBatches 相比：本节点把循环 *内联* 在自身实现里，避免在 DAG 拓扑里
 * 表达 "回环" 这种非 DAG 结构。这样保持图的纯净性，同时与 n8n 的语义等价。
 *
 * @param <T> 单条 item 的类型
 */
public final class LoopOverItemsNode<T> extends DagNode {

    private static final Logger logger = LogManager.getLogger();

    private final ItemHandler<T> handler;

    /**
     * 是否允许部分失败（true 时每条 item 失败仅记录日志；false 时立即抛异常）。
     */
    private final boolean continueOnError;

    /**
     * 节点最大处理 item 数（防止巨型工作集导致 LLM 调用爆炸）。0 表示不限制。
     */
    private final int maxItems;

    public LoopOverItemsNode(String id, ItemHandler<T> handler) {
        this(id, handler, true, 0);
    }

    public LoopOverItemsNode(String id, ItemHandler<T> handler,
                             boolean continueOnError, int maxItems) {
        super(id, "Loop Over Items");
        if (handler == null) {
            throw new IllegalArgumentException("handler required");
        }
        this.handler = handler;
        this.continueOnError = continueOnError;
        this.maxItems = Math.max(0, maxItems);
    }

    @SuppressWarnings("unchecked")
    @Override
    public NodeResult execute(DagContext ctx, List<NodeResult> inputs) throws Exception {
        if (inputs == null || inputs.isEmpty() || inputs.get(0).getData() == null) {
            return NodeResult.ok(new ArrayList<Object>());
        }
        Object data = inputs.get(0).getData();
        List<T> items;
        if (data instanceof List) {
            items = (List<T>) data;
        } else {
            items = new ArrayList<>();
            items.add((T) data);
        }
        int limit = maxItems == 0 ? items.size() : Math.min(items.size(), maxItems);
        ctx.emitLoop(new DagContext.LoopEvent(getId(), 0, limit,
                DagContext.LoopEvent.Phase.START,
                "loop start (" + limit + " items)", ""));
        List<Object> outputs = new ArrayList<>(limit);
        long t0 = System.currentTimeMillis();
        for (int i = 0; i < limit; i++) {
            if (ctx.isCancelled()) {
                break;
            }
            T item = items.get(i);
            String label = describeItem(item, i, limit);
            ctx.emit(getId(), me.n1ar4.jar.analyzer.ai.workflow.core.NodeStatus.RUNNING,
                    "loop " + (i + 1) + "/" + limit + "  " + label);
            ctx.emitLoop(new DagContext.LoopEvent(getId(), i, limit,
                    DagContext.LoopEvent.Phase.STEP, label, ""));
            long iterStart = System.currentTimeMillis();
            try {
                Object out = handler.handle(ctx, item, i, limit);
                if (out != null) {
                    outputs.add(out);
                }
                long cost = System.currentTimeMillis() - iterStart;
                ctx.emitLoop(new DagContext.LoopEvent(getId(), i, limit,
                        DagContext.LoopEvent.Phase.ITEM_DONE, label,
                        "cost=" + cost + "ms"));
            } catch (Throwable t) {
                logger.warn("loop item {} failed: {}", i, t.toString());
                ctx.emitLoop(new DagContext.LoopEvent(getId(), i, limit,
                        DagContext.LoopEvent.Phase.ITEM_FAILED, label,
                        String.valueOf(t.getMessage())));
                if (!continueOnError) {
                    throw t;
                }
            }
        }
        long total = System.currentTimeMillis() - t0;
        ctx.emit(getId(), me.n1ar4.jar.analyzer.ai.workflow.core.NodeStatus.RUNNING,
                "loop done " + outputs.size() + "/" + limit + " in " + total + "ms");
        return NodeResult.ok(outputs);
    }

    /**
     * 把 item 转为简短的可读标签。优先使用 Map 的 className/className-like 字段。
     */
    @SuppressWarnings("unchecked")
    private String describeItem(Object item, int idx, int total) {
        if (item == null) {
            return "(null)";
        }
        if (item instanceof java.util.Map) {
            java.util.Map<String, Object> m = (java.util.Map<String, Object>) item;
            for (String key : new String[]{"className", "name", "id", "title"}) {
                Object v = m.get(key);
                if (v != null) {
                    String s = String.valueOf(v);
                    return s.length() > 80 ? s.substring(0, 77) + "..." : s;
                }
            }
        }
        String s = String.valueOf(item);
        if (s.length() > 80) {
            return s.substring(0, 77) + "...";
        }
        return s;
    }

    @FunctionalInterface
    public interface ItemHandler<T> {
        /**
         * 处理单个 item，返回值会被加入 LoopOverItemsNode 的输出 List；返回 null 表示忽略该 item。
         */
        Object handle(DagContext ctx, T item, int index, int total) throws Exception;
    }
}
