/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.mcp;

/**
 * 标记当前线程是否处于 MCP 工具调用上下文中
 * <p>
 * 用途：
 * 1) 让原本会弹 GUI 对话框（JOptionPane）的 API 实现，能识别 MCP 上下文并改为抛异常 / 走静默错误路径，
 * 避免 mcp-tool 线程被模态对话框永久阻塞。
 * 2) 让会读 GUI 控件状态（isSelected 等）的代码可以提前转向稳定默认值。
 * 3) 让重活算法（DFS 等）可以周期性检查截止时间提早退出（即使 Future.cancel 不响应中断）。
 */
public final class McpContext {

    private static final ThreadLocal<Boolean> IN_MCP = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<Long> DEADLINE_MS = ThreadLocal.withInitial(() -> 0L);

    private McpContext() {
    }

    /**
     * 进入 MCP 工具调用上下文
     *
     * @param timeoutMs 任务总超时（毫秒），<=0 表示无截止时间
     */
    public static void enter(long timeoutMs) {
        IN_MCP.set(Boolean.TRUE);
        if (timeoutMs > 0) {
            DEADLINE_MS.set(System.currentTimeMillis() + timeoutMs);
        } else {
            DEADLINE_MS.set(0L);
        }
    }

    /**
     * 退出 MCP 工具调用上下文（必须在 finally 调用）
     */
    public static void leave() {
        IN_MCP.remove();
        DEADLINE_MS.remove();
    }

    /**
     * 当前线程是否处于 MCP 上下文
     */
    public static boolean isInMcp() {
        Boolean v = IN_MCP.get();
        return v != null && v;
    }

    /**
     * 是否已经超过 deadline 或被中断
     * 算法侧每隔若干层递归调用一次即可，提供"协作式中断"
     */
    public static boolean isCancelled() {
        if (Thread.currentThread().isInterrupted()) {
            return true;
        }
        long deadline = DEADLINE_MS.get();
        return deadline > 0 && System.currentTimeMillis() > deadline;
    }

    /**
     * 用于希望立即抛出的算法侧检查
     */
    public static void checkCancelled() {
        if (isCancelled()) {
            throw new McpCancelledException();
        }
    }

    /**
     * 表示 MCP 工具调用被取消或超时（协作式中断）
     */
    public static class McpCancelledException extends RuntimeException {
        public McpCancelledException() {
            super("mcp tool execution cancelled or timeout");
        }
    }
}
