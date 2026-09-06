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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 污点引擎精度修复的端到端回归测试（2026/09/06 四项修复）：
 * <ul>
 *   <li>构造器 to:this：new String(tainted) 后污点存活</li>
 *   <li>varargs：String.format 的 Object[] 元素污点传播到数组引用</li>
 *   <li>接口 default 方法：方法体正常分析而非直接透传</li>
 *   <li>ATHROW：抛出对象的污点进入 catch 块</li>
 * </ul>
 * 思路仿 TaintMethodAdapterDualSlotTest：ClassWriter 现编最小类喂给 TaintClassVisitor。
 */
class TaintEnginePrecisionTest {

    private static PropagationRuleSet propagation;

    @BeforeAll
    static void loadRules() {
        InputStream in = TaintEnginePrecisionTest.class.getClassLoader()
                .getResourceAsStream("propagation.json");
        propagation = PropagationRuleSet.loadJSON(in);
    }

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
     * 运行一跳链路验证（cur 体内调用 next=Sink.target(String)），返回出口污点。
     */
    private static TaintTransfer runHop(byte[] bytes, TaintTransfer entry,
                                        String curName, String curDesc, boolean ifaceBytes) {
        MethodReference.Handle cur = new MethodReference.Handle(
                new ClassReference.Handle(ifaceBytes ? "DefIface" : "Caller"), curName, curDesc);
        MethodReference.Handle next = new MethodReference.Handle(
                new ClassReference.Handle("Sink"), "target", "(Ljava/lang/String;)V");
        TaintTransfer exit = new TaintTransfer();
        TaintClassVisitor tcv = new TaintClassVisitor(
                entry, cur, next, exit, null, propagation, new TaintEventSink(), 0);
        ClassReader cr = new ClassReader(bytes);
        cr.accept(tcv, Const.AnalyzeASMOptions);
        return exit;
    }

    private static byte[] callerClass(Body body) {
        ClassWriter cw = new FramesCW();
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "Caller", null, "java/lang/Object", null);
        MethodVisitor c = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        c.visitCode();
        c.visitVarInsn(Opcodes.ALOAD, 0);
        c.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        c.visitInsn(Opcodes.RETURN);
        c.visitMaxs(1, 1);
        c.visitEnd();
        MethodVisitor m = cw.visitMethod(Opcodes.ACC_PUBLIC, "m", "(Ljava/lang/String;)V", null, null);
        m.visitCode();
        body.emit(m);
        m.visitMaxs(0, 0);
        m.visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    interface Body {
        void emit(MethodVisitor mv);
    }

    // ---------------- 修复 1：构造器 to:this ----------------

    @Test
    void ctorTaintSurvivesNewString() {
        // void m(String a) { Sink.target(new String(a)); }
        // String.<init> 规则 any->this：修复前 to:this 对 void 构造器是空操作，污点必丢
        byte[] bytes = callerClass(mv -> {
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/String");
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>",
                    "(Ljava/lang/String;)V", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Sink", "target",
                    "(Ljava/lang/String;)V", false);
            mv.visitInsn(Opcodes.RETURN);
        });

        TaintTransfer entry = new TaintTransfer();
        entry.markLocal(1);
        TaintTransfer exit = runHop(bytes, entry, "m", "(Ljava/lang/String;)V", false);

        assertTrue(exit.isLocalTainted(0),
                "new String(tainted) 之后污点应存活并到达下一跳（此前构造器规则失效）");
    }

    // ---------------- 修复 2：varargs 数组元素污点 ----------------

    @Test
    void varargsTaintSurvivesStringFormat() {
        // void m(String a) { Sink.target(String.format("x %s", a)); }
        // javac varargs：ANEWARRAY + DUP + AASTORE——修复前数组引用干净，污点必丢
        byte[] bytes = callerClass(mv -> {
            mv.visitLdcInsn("x %s");
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            mv.visitInsn(Opcodes.DUP);
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInsn(Opcodes.AASTORE);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "format",
                    "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Sink", "target",
                    "(Ljava/lang/String;)V", false);
            mv.visitInsn(Opcodes.RETURN);
        });

        TaintTransfer entry = new TaintTransfer();
        entry.markLocal(1);
        TaintTransfer exit = runHop(bytes, entry, "m", "(Ljava/lang/String;)V", false);

        assertTrue(exit.isLocalTainted(0),
                "String.format 的 varargs 数组元素污点应传播到下一跳（此前 varargs 必断链）");
    }

    // ---------------- 修复 3：接口 default 方法 ----------------

    @Test
    void defaultMethodBodyAnalyzed() {
        // interface DefIface { default void m(String a) { Sink.target(a); } }
        // 修复前：接口一律透传，default 方法体不分析
        ClassWriter cw = new FramesCW();
        cw.visit(Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                "DefIface", null, "java/lang/Object", null);
        MethodVisitor m = cw.visitMethod(Opcodes.ACC_PUBLIC, "m",
                "(Ljava/lang/String;)V", null, null);
        m.visitCode();
        m.visitVarInsn(Opcodes.ALOAD, 1);
        m.visitMethodInsn(Opcodes.INVOKESTATIC, "Sink", "target",
                "(Ljava/lang/String;)V", false);
        m.visitInsn(Opcodes.RETURN);
        m.visitMaxs(0, 0);
        m.visitEnd();
        cw.visitEnd();

        TaintTransfer entry = new TaintTransfer();
        entry.markLocal(1);
        TaintTransfer exit = runHop(cw.toByteArray(), entry, "m", "(Ljava/lang/String;)V", true);

        assertTrue(exit.isLocalTainted(0), "default 方法体应被分析（而非透传）");
    }

    @Test
    void abstractInterfaceStillPassthrough() {
        // interface DefIface { void m(String a); } —— 无方法体，仍走透传（回归保护）
        ClassWriter cw = new FramesCW();
        cw.visit(Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT,
                "DefIface", null, "java/lang/Object", null);
        cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "m",
                "(Ljava/lang/String;)V", null, null);
        cw.visitEnd();

        TaintTransfer entry = new TaintTransfer();
        entry.markLocal(1);
        TaintTransfer exit = runHop(cw.toByteArray(), entry, "m", "(Ljava/lang/String;)V", true);

        assertTrue(exit.isLocalTainted(1),
                "抽象接口方法仍应按槽位透传（出口槽 1 对应入口槽 1）");
        assertFalse(exit.isLocalTainted(0), "透传不应把污点放到 this 槽");
    }

    // ---------------- 修复 4：ATHROW 异常对象污点进入 catch ----------------

    @Test
    void thrownTaintReachesCatchBlock() {
        // try { throw a; } catch (e) { Sink.target(e); }（类型仅为模拟所需，污点语义成立）
        byte[] bytes = callerClass(mv -> {
            Label start = new Label();
            Label end = new Label();
            Label handler = new Label();
            mv.visitTryCatchBlock(start, end, handler, null);
            mv.visitLabel(start);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitInsn(Opcodes.ATHROW);
            mv.visitLabel(end);
            mv.visitLabel(handler);
            mv.visitVarInsn(Opcodes.ASTORE, 2);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Sink", "target",
                    "(Ljava/lang/String;)V", false);
            mv.visitInsn(Opcodes.RETURN);
        });

        TaintTransfer entry = new TaintTransfer();
        entry.markLocal(1);
        TaintTransfer exit = runHop(bytes, entry, "m", "(Ljava/lang/String;)V", false);

        assertTrue(exit.isLocalTainted(0),
                "抛出对象的污点应进入 catch 块并到达下一跳（此前 ATHROW 直接丢弃）");
    }
}
