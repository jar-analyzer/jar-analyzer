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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 工作流执行上下文。
 * <p>
 * 提供全局常量（GlobalConstants）、节点输出存储、取消信号、共享状态。
 * <p>
 * 安全注意：此处不要存储 API Key 等敏感信息——LLM 凭据通过 {@code AIConfigManager} 直接读取。
 */
public final class DagContext {

    /**
     * 工作流级别的全局常量（对应 n8n 的 Global Constants）。
     */
    private final Map<String, String> constants = new HashMap<>();

    /**
     * 每个节点产出的最近一次结果。
     */
    private final Map<String, NodeResult> nodeOutputs = new ConcurrentHashMap<>();

    /**
     * 节点级用户态变量。
     */
    private final Map<String, Object> sharedState = new ConcurrentHashMap<>();

    /**
     * 取消标志：true 时所有未启动的节点直接进入 CANCELLED。
     */
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * 进度监听器（可选），由调用方注册。
     */
    private volatile ProgressListener progressListener;

    /**
     * Loop 迭代监听器（可选）。
     */
    private volatile LoopListener loopListener;

    public Map<String, String> getConstants() {
        return constants;
    }

    public DagContext putConstant(String k, String v) {
        if (k != null && v != null) {
            this.constants.put(k, v);
        }
        return this;
    }

    public String constant(String k) {
        return this.constants.get(k);
    }

    public Map<String, NodeResult> getNodeOutputs() {
        return Collections.unmodifiableMap(nodeOutputs);
    }

    public NodeResult getOutput(String nodeId) {
        return nodeOutputs.get(nodeId);
    }

    void putOutput(String nodeId, NodeResult r) {
        nodeOutputs.put(nodeId, r);
    }

    public Map<String, Object> getSharedState() {
        return sharedState;
    }

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public LoopListener getLoopListener() {
        return loopListener;
    }

    public void setLoopListener(LoopListener loopListener) {
        this.loopListener = loopListener;
    }

    public void emit(String nodeId, NodeStatus status, String message) {
        ProgressListener pl = this.progressListener;
        if (pl != null) {
            try {
                pl.onProgress(nodeId, status, message);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * 触发一次 loop 事件回调（不会抛出，回调内部异常被吞）。
     */
    public void emitLoop(LoopEvent event) {
        LoopListener ll = this.loopListener;
        if (ll != null && event != null) {
            try {
                ll.onLoopEvent(event);
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * 简单的取多输入合并工具：把若干 NodeResult 的 data 拍平成一个 List。
     */
    public static List<Object> flatten(List<NodeResult> inputs) {
        List<Object> out = new ArrayList<>();
        if (inputs == null) {
            return out;
        }
        for (NodeResult r : inputs) {
            if (r == null || r.getData() == null) {
                continue;
            }
            Object d = r.getData();
            if (d instanceof List) {
                for (Object o : (List<?>) d) {
                    if (o != null) {
                        out.add(o);
                    }
                }
            } else {
                out.add(d);
            }
        }
        return out;
    }

    public interface ProgressListener {
        void onProgress(String nodeId, NodeStatus status, String message);
    }

    /**
     * Loop 迭代事件回调。
     */
    public interface LoopListener {
        void onLoopEvent(LoopEvent event);
    }

    /**
     * Loop 单次迭代事件。所有字段对 GUI 都是只读的。
     */
    public static final class LoopEvent {
        public enum Phase {
            START,
            STEP,
            ITEM_DONE,
            ITEM_FAILED
        }

        private final String loopNodeId;
        private final int index;
        private final int total;
        private final Phase phase;
        private final String label;
        private final String detail;
        private final long timestamp = System.currentTimeMillis();

        public LoopEvent(String loopNodeId, int index, int total,
                         Phase phase, String label, String detail) {
            this.loopNodeId = loopNodeId;
            this.index = index;
            this.total = total;
            this.phase = phase == null ? Phase.STEP : phase;
            this.label = label == null ? "" : label;
            this.detail = detail == null ? "" : detail;
        }

        public String getLoopNodeId() {
            return loopNodeId;
        }

        public int getIndex() {
            return index;
        }

        public int getTotal() {
            return total;
        }

        public Phase getPhase() {
            return phase;
        }

        public String getLabel() {
            return label;
        }

        public String getDetail() {
            return detail;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
