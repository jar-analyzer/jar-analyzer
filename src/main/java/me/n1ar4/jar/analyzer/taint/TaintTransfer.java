/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import java.util.BitSet;

/**
 * 方法间污点传递载荷。
 * <p>
 * 旧实现使用 {@code AtomicInteger pass} 仅记录"上一调用把污点传到了下一方法的第几个参数"，
 * 这导致：
 * <ul>
 *   <li>无法表达"同时污染了多个参数（含 this）"</li>
 *   <li>难以表达"返回值带污点、调用者把它存为本地变量再传递"的场景</li>
 *   <li>this 与第 0 个参数索引重叠，需要散落各处的 +1/-1 修正，已经踩过坑</li>
 * </ul>
 * <p>
 * 本载荷采用统一的 <b>locals 索引语义</b>：
 * <ul>
 *   <li>对非 static 方法：index 0 表示 {@code this}，index 1..N 表示形参 arg0..arg(N-1)</li>
 *   <li>对 static 方法：index 0..N-1 表示形参 arg0..arg(N-1)</li>
 * </ul>
 * 即与 JVM 局部变量表第一段的语义完全一致，避免了"参数索引"与"locals 索引"的反复换算。
 * <p>
 * 该类被设计为"沿调用链单线程使用"，并发场景请克隆后传递。
 */
public final class TaintTransfer {

    /**
     * 调用 next 时，next 的哪些 locals 索引被污染（语义见类注释）。
     */
    private final BitSet taintedLocals = new BitSet();

    /**
     * 调用 next 后，其返回值是否带污点。
     * 当前实现暂未在 caller 侧消费此字段，但在 callee 内已经按"任意输入污染 → 返回值染色"传播。
     * 字段存在以便后续把"返回值进一步流向其它语句"加入分析。
     */
    private boolean returnTainted = false;

    public TaintTransfer() {
    }

    /**
     * 是否在本次调用中传递了任何污点信息。
     */
    public boolean hasTaint() {
        return !taintedLocals.isEmpty() || returnTainted;
    }

    /**
     * 标记 locals 索引 i 为污染。
     */
    public void markLocal(int localIndex) {
        if (localIndex < 0) {
            return;
        }
        taintedLocals.set(localIndex);
    }

    public boolean isLocalTainted(int localIndex) {
        if (localIndex < 0) {
            return false;
        }
        return taintedLocals.get(localIndex);
    }

    public BitSet getTaintedLocals() {
        return taintedLocals;
    }

    public boolean isReturnTainted() {
        return returnTainted;
    }

    public void setReturnTainted(boolean returnTainted) {
        this.returnTainted = returnTainted;
    }

    /**
     * 清空全部状态。
     */
    public void reset() {
        taintedLocals.clear();
        returnTainted = false;
    }

    /**
     * 用于跨链路克隆。
     */
    public TaintTransfer copy() {
        TaintTransfer t = new TaintTransfer();
        t.taintedLocals.or(this.taintedLocals);
        t.returnTainted = this.returnTainted;
        return t;
    }

    @Override
    public String toString() {
        return "TaintTransfer{locals=" + taintedLocals + ", retTainted=" + returnTainted + "}";
    }
}
