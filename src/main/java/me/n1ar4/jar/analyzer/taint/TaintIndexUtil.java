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
 * 跨方法污点分析中"locals 索引"与"调用前栈位置"之间的双向映射。
 * <p>
 * Locals 索引语义（与 JVM 第一段 locals 一致）：
 * <ul>
 *   <li>非 static：0 = this，1..N = arg0..arg(N-1)</li>
 *   <li>static：0..N-1 = arg0..arg(N-1)</li>
 * </ul>
 * 调用前栈语义：栈顶到栈底依次是 last arg, ..., first arg, [this]。
 * 即 paramSeq=0（含 this 的最底参数）距栈顶 (argCount-1) slot；
 * paramSeq=argCount-1（最顶参数）距栈顶 0 slot。
 * <p>
 * 注意：本工具不处理 LONG/DOUBLE 双 slot 的情况——
 * JVMRuntimeAdapter 已经在栈上为它们维护两个 slot，但污点泛型 String 场景下
 * 上层只关心"这个参数槽是否染色"，因此按"参数序号"而非"slot 序号"建模即可。
 * 如果未来要做 long/double 精细传播，需要在此扩展。
 */
final class TaintIndexUtil {

    private TaintIndexUtil() {
    }

    /**
     * 调用前 callee 的参数总数（含 this）。
     */
    static int calleeArgCount(int invokeOpcode, String calleeDesc) {
        int n = Type.getArgumentTypes(calleeDesc).length;
        if (invokeOpcode != Opcodes.INVOKESTATIC) {
            n += 1;
        }
        return n;
    }

    /**
     * 把 callee 视角的"locals 索引"映射成"调用前栈顶往下数第几个 slot（自顶向下）"。
     * 返回 -1 表示该 localIndex 不在本次调用入参范围内。
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
     * callee 视角的 locals 索引。
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
