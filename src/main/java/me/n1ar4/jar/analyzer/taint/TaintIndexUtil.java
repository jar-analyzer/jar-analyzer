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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 跨方法污点分析中"locals 槽位"与"调用前栈位置"之间的双向映射。
 * <p>
 * Locals 槽位语义（与 JVM 第一段 locals 完全一致，含双 slot 类型）：
 * <ul>
 *   <li>非 static：0 = this，随后按参数顺序每个参数占 {@link Type#getSize()} 个槽
 *       （long/double 占 2 个槽，低位在前）</li>
 *   <li>static：0 起按参数顺序占槽</li>
 * </ul>
 * 调用前栈语义：receiver（若有）在最底，随后按参数顺序入栈，每个参数同样按
 * size 占槽；栈顶到栈底倒数第 k 个 slot（k 从 0 计）。
 * <p>
 * 2026/09/06 修复：旧实现按"参数序数"而非"slot 布局"换算，当 callee 形参中
 * 存在 long/double 时其后所有参数的映射都会错位（例：static foo(long, String)
 * 的 String 实际在 local 2，旧算法给出 local 1 即 long 高位槽）。栈与 locals
 * 对双 slot 值均为"低位在前"，因此按 slot 总数线性换算对双 slot 参数同样精确。
 */
final class TaintIndexUtil {

    private TaintIndexUtil() {
    }

    /**
     * 调用前 callee 的参数总 slot 数（含 this；long/double 每个占 2 slot）。
     */
    static int calleeArgCount(int invokeOpcode, String calleeDesc) {
        int n = 0;
        for (Type t : Type.getArgumentTypes(calleeDesc)) {
            n += t.getSize();
        }
        if (invokeOpcode != Opcodes.INVOKESTATIC) {
            n += 1;
        }
        return n;
    }

    /**
     * 把 callee 视角的"locals 槽位"映射成"调用前栈顶往下数第几个 slot（自顶向下）"。
     * 返回 -1 表示该槽位不在本次调用入参范围内。
     */
    static int localIndexToStackOffsetFromTop(int invokeOpcode, String calleeDesc, int localIndex) {
        int argCount = calleeArgCount(invokeOpcode, calleeDesc);
        int p = localIndex;
        if (p < 0 || p >= argCount) {
            return -1;
        }
        return argCount - 1 - p;
    }

    /**
     * 反向：把"调用前栈顶往下数第 stackOffsetFromTop 个 slot"映射回
     * callee 视角的 locals 槽位。
     */
    static int stackOffsetFromTopToCalleeLocalIndex(int invokeOpcode, String calleeDesc, int stackOffsetFromTop) {
        int argCount = calleeArgCount(invokeOpcode, calleeDesc);
        int p = argCount - 1 - stackOffsetFromTop;
        if (p < 0 || p >= argCount) {
            return -1;
        }
        return p;
    }
}
