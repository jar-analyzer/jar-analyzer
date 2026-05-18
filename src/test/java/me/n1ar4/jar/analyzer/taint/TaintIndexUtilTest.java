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

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * locals 索引 ↔ 调用前栈位置 互转的边界测试。
 * <p>
 * 这一段在历史实现中是污点分析最容易出错的地方：
 * 旧 TaintMethodAdapter 中的 paramIndex 计算需要 +1/-1 修正，作者已经踩过坑（注释 "// 处理 0"）。
 * 这些测试针对静态/非静态、单参/多参/0 参等组合做精确锚定。
 */
class TaintIndexUtilTest {

    // ---------------- argCount 基础 ----------------

    @Test
    void argCount_static_noArgs() {
        assertEquals(0, TaintIndexUtil.calleeArgCount(Opcodes.INVOKESTATIC, "()V"));
    }

    @Test
    void argCount_static_threeArgs() {
        assertEquals(3, TaintIndexUtil.calleeArgCount(
                Opcodes.INVOKESTATIC, "(Ljava/lang/String;ILjava/util/Map;)V"));
    }

    @Test
    void argCount_virtual_noArgs_includesThis() {
        // INVOKEVIRTUAL 的 "()V" 算入 this -> 1
        assertEquals(1, TaintIndexUtil.calleeArgCount(Opcodes.INVOKEVIRTUAL, "()V"));
    }

    @Test
    void argCount_virtual_twoArgs_includesThis() {
        assertEquals(3, TaintIndexUtil.calleeArgCount(
                Opcodes.INVOKEVIRTUAL, "(Ljava/lang/String;I)V"));
    }

    @Test
    void argCount_interface_includesThis() {
        assertEquals(2, TaintIndexUtil.calleeArgCount(
                Opcodes.INVOKEINTERFACE, "(Ljava/lang/Object;)V"));
    }

    // ---------------- localIndex -> stackOffsetFromTop ----------------

    @Test
    void localToStack_static_oneArg_localIs0_atTop() {
        // static foo(String) -> arg 在栈顶
        int off = TaintIndexUtil.localIndexToStackOffsetFromTop(
                Opcodes.INVOKESTATIC, "(Ljava/lang/String;)V", 0);
        assertEquals(0, off);
    }

    @Test
    void localToStack_static_threeArgs_arg0_atBottom() {
        // static foo(a, b, c)：locals 索引 0=a，调用前栈底
        int off = TaintIndexUtil.localIndexToStackOffsetFromTop(
                Opcodes.INVOKESTATIC, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 0);
        // 栈：c (top), b, a -> a 距栈顶 2
        assertEquals(2, off);
    }

    @Test
    void localToStack_static_threeArgs_argLast_atTop() {
        int off = TaintIndexUtil.localIndexToStackOffsetFromTop(
                Opcodes.INVOKESTATIC, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 2);
        assertEquals(0, off);
    }

    @Test
    void localToStack_virtual_thisAtBottom() {
        // virtual foo(a)：locals 0 = this，栈：a (top), this -> this 距顶 1
        int off = TaintIndexUtil.localIndexToStackOffsetFromTop(
                Opcodes.INVOKEVIRTUAL, "(Ljava/lang/String;)V", 0);
        assertEquals(1, off);
    }

    @Test
    void localToStack_virtual_arg_atTop() {
        // virtual foo(a)：locals 1 = a，栈顶
        int off = TaintIndexUtil.localIndexToStackOffsetFromTop(
                Opcodes.INVOKEVIRTUAL, "(Ljava/lang/String;)V", 1);
        assertEquals(0, off);
    }

    @Test
    void localToStack_virtual_zeroArg_thisAtTop() {
        // virtual foo()：locals 0 = this，唯一 slot 即栈顶
        int off = TaintIndexUtil.localIndexToStackOffsetFromTop(
                Opcodes.INVOKEVIRTUAL, "()V", 0);
        assertEquals(0, off);
    }

    @Test
    void localToStack_outOfRange_returnsMinusOne() {
        int off = TaintIndexUtil.localIndexToStackOffsetFromTop(
                Opcodes.INVOKESTATIC, "(I)V", 5);
        assertEquals(-1, off);
    }

    @Test
    void localToStack_negative_returnsMinusOne() {
        int off = TaintIndexUtil.localIndexToStackOffsetFromTop(
                Opcodes.INVOKESTATIC, "(I)V", -1);
        assertEquals(-1, off);
    }

    // ---------------- stackOffsetFromTop -> localIndex ----------------

    @Test
    void stackToLocal_static_top_isLastArg() {
        int local = TaintIndexUtil.stackOffsetFromTopToCalleeLocalIndex(
                Opcodes.INVOKESTATIC, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 0);
        assertEquals(2, local); // 最顶是 arg2 -> locals[2]
    }

    @Test
    void stackToLocal_static_bottom_isFirstArg() {
        int local = TaintIndexUtil.stackOffsetFromTopToCalleeLocalIndex(
                Opcodes.INVOKESTATIC, "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", 2);
        assertEquals(0, local);
    }

    @Test
    void stackToLocal_virtual_top_isLastArg() {
        // virtual foo(a, b)：栈 b(top), a, this -> 顶是 b 对应 locals[2]
        int local = TaintIndexUtil.stackOffsetFromTopToCalleeLocalIndex(
                Opcodes.INVOKEVIRTUAL, "(Ljava/lang/String;Ljava/lang/String;)V", 0);
        assertEquals(2, local);
    }

    @Test
    void stackToLocal_virtual_bottom_isThis() {
        int local = TaintIndexUtil.stackOffsetFromTopToCalleeLocalIndex(
                Opcodes.INVOKEVIRTUAL, "(Ljava/lang/String;Ljava/lang/String;)V", 2);
        assertEquals(0, local);
    }

    @Test
    void stackToLocal_outOfRange_returnsMinusOne() {
        int local = TaintIndexUtil.stackOffsetFromTopToCalleeLocalIndex(
                Opcodes.INVOKESTATIC, "(I)V", 5);
        assertEquals(-1, local);
    }

    // ---------------- 双向往返 ----------------

    @Test
    void roundTrip_static_allArgs() {
        String desc = "(Ljava/lang/String;ILjava/util/Map;Ljava/lang/Object;)V";
        for (int local = 0; local < 4; local++) {
            int off = TaintIndexUtil.localIndexToStackOffsetFromTop(Opcodes.INVOKESTATIC, desc, local);
            int back = TaintIndexUtil.stackOffsetFromTopToCalleeLocalIndex(Opcodes.INVOKESTATIC, desc, off);
            assertEquals(local, back, "static round-trip at local=" + local);
        }
    }

    @Test
    void roundTrip_virtual_allArgs() {
        String desc = "(Ljava/lang/String;I)V";
        // locals 索引：0=this, 1=arg0, 2=arg1
        for (int local = 0; local < 3; local++) {
            int off = TaintIndexUtil.localIndexToStackOffsetFromTop(Opcodes.INVOKEVIRTUAL, desc, local);
            int back = TaintIndexUtil.stackOffsetFromTopToCalleeLocalIndex(Opcodes.INVOKEVIRTUAL, desc, off);
            assertEquals(local, back, "virtual round-trip at local=" + local);
        }
    }
}
