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

import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.starter.Const;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 双 slot（long/double）形参场景下链路跳数（Case 1 命中 next）的端到端回归测试。
 * <p>
 * 2026/09/06 修复前：callee 形参 (long, String) 时 String 的污点被投递到
 * long 的高位槽（错位一槽），跨方法传播丢失。修复后按 slot 布局精确投递。
 * 思路仿 JVMRuntimeAdapterTest：ClassWriter 现编最小类喂给 TaintClassVisitor。
 */
class TaintMethodAdapterDualSlotTest {

    private static final String NEXT_DESC = "(JLjava/lang/String;)V";

    static class FramesCW extends ClassWriter {
        FramesCW() {
            super(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            return "java/lang/Object";
        }
    }

    /**
     * 运行一跳链路验证（cur 方法体内调用 next），返回出口污点。
     */
    private static TaintTransfer runHop(byte[] bytes, TaintTransfer entry,
                                        String curName, String curDesc) {
        MethodReference.Handle cur = new MethodReference.Handle(
                new ClassReference.Handle("Caller"), curName, curDesc);
        MethodReference.Handle next = new MethodReference.Handle(
                new ClassReference.Handle("Sink"), "target", NEXT_DESC);
        TaintTransfer exit = new TaintTransfer();
        TaintClassVisitor tcv = new TaintClassVisitor(
                entry, cur, next, exit, null, null, new TaintEventSink(), 0);
        ClassReader cr = new ClassReader(bytes);
        cr.accept(tcv, Const.AnalyzeASMOptions);
        return exit;
    }

    @Test
    void staticHop_stringAfterLongLandsOnSlot2() {
        // static void m(String a) { Sink.target(1L, a); }
        // callee foo(long, String)：String 实际在 locals[2]（J 占 0-1）
        ClassWriter cw = new FramesCW();
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Caller", null, "java/lang/Object", null);
        MethodVisitor m = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "m", "(Ljava/lang/String;)V", null, null);
        m.visitCode();
        m.visitLdcInsn(1L);
        m.visitVarInsn(Opcodes.ALOAD, 0);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Sink", "target", NEXT_DESC, false);
        m.visitInsn(Opcodes.RETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
        cw.visitEnd();

        TaintTransfer entry = new TaintTransfer();
        entry.markLocal(0);
        TaintTransfer exit = runHop(cw.toByteArray(), entry, "m", "(Ljava/lang/String;)V");

        assertTrue(exit.isLocalTainted(2),
                "long 双 slot 之后 String 污点应投递到 callee locals 第 2 槽");
        assertFalse(exit.isLocalTainted(1), "不应误染 long 高位槽");
        assertFalse(exit.isLocalTainted(0), "不应误染 long 低位槽");
    }

    @Test
    void virtualHop_stringAfterLongLandsOnSlot3() {
        // void m(String a) { ((Sink) null).target(1L, a); }
        // virtual foo(long, String)：this=0, J=1-2, String=3
        ClassWriter cw = new FramesCW();
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Caller", null, "java/lang/Object", null);
        MethodVisitor c = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        c.visitCode();
        c.visitVarInsn(Opcodes.ALOAD, 0);
        c.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        c.visitInsn(Opcodes.RETURN);
        c.visitMaxs(1, 1);
        c.visitEnd();
        MethodVisitor m = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "m", "(Ljava/lang/String;)V", null, null);
        m.visitCode();
        m.visitInsn(Opcodes.ACONST_NULL);
        m.visitLdcInsn(1L);
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Sink", "target", NEXT_DESC, false);
        m.visitInsn(Opcodes.RETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
        cw.visitEnd();

        TaintTransfer entry = new TaintTransfer();
        entry.markLocal(1);
        TaintTransfer exit = runHop(cw.toByteArray(), entry, "m", "(Ljava/lang/String;)V");

        assertTrue(exit.isLocalTainted(3),
                "virtual 场景 long 双 slot 之后 String 应投递到 callee locals 第 3 槽");
        assertFalse(exit.isLocalTainted(2), "不应误染 long 高位槽");
    }
}
