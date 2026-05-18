/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint.jvm;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AnalyzerAdapter;

import java.util.*;

/**
 * 模拟栈帧核心类
 * 目标MethodAdapter需要继承该类
 *
 * @param <T> 污点泛型
 */
@SuppressWarnings("all")
public class JVMRuntimeAdapter<T> extends MethodVisitor {
    private final AnalyzerAdapter analyzerAdapter;
    private final int access;
    private final String desc;

    private final Map<Label, GotoState<T>> gotoStates = new HashMap<>();
    private final Set<Label> exceptionHandlerLabels = new HashSet<>();

    protected OperandStack<T> operandStack;
    protected LocalVariables<T> localVariables;

    public JVMRuntimeAdapter(final int api, final MethodVisitor mv, final String owner,
                             int access, String name, String desc) {
        super(api, new AnalyzerAdapter(owner, access, name, desc, mv));
        this.analyzerAdapter = (AnalyzerAdapter) this.mv;
        this.access = access;
        this.desc = desc;
        operandStack = new OperandStack<>();
        localVariables = new LocalVariables<>();
    }

    private void sanityCheck() {
        if (analyzerAdapter.stack != null && operandStack.size() != analyzerAdapter.stack.size()) {
            throw new IllegalStateException("bad stack size");
        }
    }

    private void mergeGotoState(Label label) {
        if (gotoStates.containsKey(label)) {
            GotoState<T> state = gotoStates.get(label);
            // old -> label
            LocalVariables<T> oldLocalVariables = state.getLocalVariables();
            OperandStack<T> oldOperandStack = state.getOperandStack();
            // new -> null
            LocalVariables<T> newLocalVariables = new LocalVariables<>();
            OperandStack<T> newOperandStack = new OperandStack<>();
            // init new
            for (Set<T> original : oldLocalVariables.getList()) {
                newLocalVariables.add(new HashSet<>(original));
            }
            for (Set<T> original : oldOperandStack.getList()) {
                newOperandStack.add(new HashSet<>(original));
            }
            // add current state
            for (int i = 0; i < localVariables.size(); i++) {
                while (newLocalVariables.size() <= i) {
                    newLocalVariables.add(new HashSet<>());
                }
                newLocalVariables.get(i).addAll(localVariables.get(i));
            }
            for (int i = 0; i < operandStack.size(); i++) {
                while (newOperandStack.size() <= i) {
                    newOperandStack.add(new HashSet<>());
                }
                newOperandStack.get(i).addAll(operandStack.get(i));
            }
            // set new state
            GotoState<T> newGotoState = new GotoState<>();
            newGotoState.setOperandStack(newOperandStack);
            newGotoState.setLocalVariables(newLocalVariables);
            gotoStates.put(label, newGotoState);
        } else {
            LocalVariables<T> oldLocalVariables = localVariables;
            OperandStack<T> oldOperandStack = operandStack;
            // new -> null
            LocalVariables<T> newLocalVariables = new LocalVariables<>();
            OperandStack<T> newOperandStack = new OperandStack<>();
            // init new
            for (Set<T> original : oldLocalVariables.getList()) {
                newLocalVariables.add(new HashSet<>(original));
            }
            for (Set<T> original : oldOperandStack.getList()) {
                newOperandStack.add(new HashSet<>(original));
            }
            // set new state
            GotoState<T> newGotoState = new GotoState<>();
            newGotoState.setOperandStack(newOperandStack);
            newGotoState.setLocalVariables(newLocalVariables);
            gotoStates.put(label, newGotoState);
        }
    }

    @Override
    public void visitCode() {
        super.visitCode();
        localVariables.clear();
        operandStack.clear();

        if ((this.access & Opcodes.ACC_STATIC) == 0) {
            localVariables.add(new HashSet<>());
        }
        for (Type argType : Type.getArgumentTypes(desc)) {
            for (int i = 0; i < argType.getSize(); i++) {
                localVariables.add(new HashSet<>());
            }
        }
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
        int stackSize = 0;
        for (int i = 0; i < nStack; i++) {
            Object typ = stack[i];
            int objectSize = 1;
            if (typ.equals(Opcodes.LONG) || typ.equals(Opcodes.DOUBLE)) {
                objectSize = 2;
            }
            for (int j = operandStack.size(); j < stackSize + objectSize; j++) {
                operandStack.add(new HashSet<>());
            }
            stackSize += objectSize;
        }
        int localSize = 0;
        for (int i = 0; i < nLocal; i++) {
            Object typ = local[i];
            int objectSize = 1;
            if (typ.equals(Opcodes.LONG) || typ.equals(Opcodes.DOUBLE)) {
                objectSize = 2;
            }
            for (int j = localVariables.size(); j < localSize + objectSize; j++) {
                localVariables.add(new HashSet<>());
            }
            localSize += objectSize;
        }
        for (int i = operandStack.size() - stackSize; i > 0; i--) {
            operandStack.remove(operandStack.size() - 1);
        }
        for (int i = localVariables.size() - localSize; i > 0; i--) {
            localVariables.remove(localVariables.size() - 1);
        }
        super.visitFrame(type, nLocal, local, nStack, stack);
        sanityCheck();
    }

    @Override
    public void visitInsn(int opcode) {
        Set<T> saved0, saved1, saved2, saved3;
        sanityCheck();
        switch (opcode) {
            case Opcodes.NOP:
                break;
            case Opcodes.ACONST_NULL:
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
                operandStack.push();
                break;
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.IALOAD:
            case Opcodes.FALOAD:
            case Opcodes.AALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LALOAD:
            case Opcodes.DALOAD:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.IASTORE:
            case Opcodes.FASTORE:
            case Opcodes.AASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.LASTORE:
            case Opcodes.DASTORE:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.POP:
                operandStack.pop();
                break;
            case Opcodes.POP2:
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.DUP:
                // 深拷贝以避免 Set 别名（多个槽位指向同一个 Set，
                // 后续任何 mutation 都会跨槽传染）。
                operandStack.push(new HashSet<>(operandStack.get(0)));
                break;
            case Opcodes.DUP_X1:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                operandStack.push(new HashSet<>(saved0));
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP_X2:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                operandStack.push(new HashSet<>(saved0));
                operandStack.push(saved2);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP2:
                // JVM 规范 DUP2:
                //   form 1 (两个 1-slot)：..., v2, v1 -> ..., v2, v1, v2, v1
                //   form 2 (一个 2-slot)：..., v        -> ..., v, v
                // 旧实现两次 get(1)，第二次 get 读到的是第一次 push 进去的元素，
                // 导致结果错位为 ..., v2, v1, v2, v2。这里先把两个值取出来再 push。
            {
                Set<T> v1 = operandStack.get(0);
                Set<T> v2 = operandStack.get(1);
                operandStack.push(new HashSet<>(v2));
                operandStack.push(new HashSet<>(v1));
            }
            break;
            case Opcodes.DUP2_X1:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                operandStack.push(new HashSet<>(saved1));
                operandStack.push(new HashSet<>(saved0));
                operandStack.push(saved2);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP2_X2:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                saved3 = operandStack.pop();
                operandStack.push(new HashSet<>(saved1));
                operandStack.push(new HashSet<>(saved0));
                operandStack.push(saved3);
                operandStack.push(saved2);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.SWAP:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                operandStack.push(saved0);
                operandStack.push(saved1);
                break;
            case Opcodes.IADD:
            case Opcodes.FADD:
            case Opcodes.ISUB:
            case Opcodes.FSUB:
            case Opcodes.IMUL:
            case Opcodes.FMUL:
            case Opcodes.IDIV:
            case Opcodes.FDIV:
            case Opcodes.IREM:
            case Opcodes.FREM:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LADD:
            case Opcodes.DADD:
            case Opcodes.LSUB:
            case Opcodes.DSUB:
            case Opcodes.LMUL:
            case Opcodes.DMUL:
            case Opcodes.LDIV:
            case Opcodes.DDIV:
            case Opcodes.LREM:
            case Opcodes.DREM:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.INEG:
            case Opcodes.FNEG:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LNEG:
            case Opcodes.DNEG:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.ISHL:
            case Opcodes.ISHR:
            case Opcodes.IUSHR:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.IAND:
            case Opcodes.IOR:
            case Opcodes.IXOR:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LAND:
            case Opcodes.LOR:
            case Opcodes.LXOR:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
            case Opcodes.I2F:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.I2L:
            case Opcodes.I2D:
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.L2I:
            case Opcodes.L2F:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.D2L:
            case Opcodes.L2D:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.F2I:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.F2L:
            case Opcodes.F2D:
                operandStack.pop();
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.D2I:
            case Opcodes.D2F:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.LCMP:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
                operandStack.pop();
                break;
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.RETURN:
                break;
            case Opcodes.ARRAYLENGTH:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.ATHROW:
                operandStack.pop();
                break;
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
                operandStack.pop();
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitInsn(opcode);
        sanityCheck();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        switch (opcode) {
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                operandStack.push();
                break;
            case Opcodes.NEWARRAY:
                operandStack.pop();
                operandStack.push();
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitIntInsn(opcode, operand);
        sanityCheck();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        for (int i = localVariables.size(); i <= var + 1; i++) {
            // 给 var+1 也预留槽位，方便 LSTORE/DSTORE 维护"高位"占位
            localVariables.add(new HashSet<>());
        }
        Set<T> saved0;
        switch (opcode) {
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
                operandStack.push(new HashSet<>(localVariables.get(var)));
                break;
            case Opcodes.LLOAD:
            case Opcodes.DLOAD:
                // 保留 var 的污点到 low slot；high slot 用空 set 占位
                operandStack.push(new HashSet<>(localVariables.get(var)));
                operandStack.push();
                break;
            case Opcodes.ALOAD:
                operandStack.push(new HashSet<>(localVariables.get(var)));
                break;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
                localVariables.set(var, operandStack.pop());
                break;
            case Opcodes.DSTORE:
            case Opcodes.LSTORE:
                // 栈上是 [..., low, high]，pop 顺序是 high -> low
                operandStack.pop();                     // high (long/double 的高位 slot 无污点意义)
                saved0 = operandStack.pop();            // low：保留污点
                localVariables.set(var, saved0);
                // var+1 仍然作为占位（second word），不带独立污点
                if (var + 1 < localVariables.size()) {
                    localVariables.set(var + 1, new HashSet<>());
                }
                break;
            case Opcodes.ASTORE:
                saved0 = operandStack.pop();
                localVariables.set(var, saved0);
                break;
            case Opcodes.RET:
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitVarInsn(opcode, var);
        sanityCheck();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        switch (opcode) {
            case Opcodes.NEW:
                operandStack.push();
                break;
            case Opcodes.ANEWARRAY:
                operandStack.pop();
                operandStack.push();
                break;
            case Opcodes.CHECKCAST:
                break;
            case Opcodes.INSTANCEOF:
                operandStack.pop();
                operandStack.push();
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitTypeInsn(opcode, type);
        sanityCheck();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        int typeSize = Type.getType(desc).getSize();
        switch (opcode) {
            case Opcodes.GETSTATIC:
                for (int i = 0; i < typeSize; i++) {
                    operandStack.push();
                }
                break;
            case Opcodes.PUTSTATIC:
                for (int i = 0; i < typeSize; i++) {
                    operandStack.pop();
                }
                break;
            case Opcodes.GETFIELD:
                operandStack.pop();
                for (int i = 0; i < typeSize; i++) {
                    operandStack.push();
                }
                break;
            case Opcodes.PUTFIELD:
                for (int i = 0; i < typeSize; i++) {
                    operandStack.pop();
                }
                operandStack.pop();
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitFieldInsn(opcode, owner, name, desc);
        sanityCheck();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        Type[] argTypes = Type.getArgumentTypes(desc);
        if (opcode != Opcodes.INVOKESTATIC) {
            Type[] extendedArgTypes = new Type[argTypes.length + 1];
            System.arraycopy(argTypes, 0, extendedArgTypes, 1, argTypes.length);
            extendedArgTypes[0] = Type.getObjectType(owner);
            argTypes = extendedArgTypes;
        }
        final Type returnType = Type.getReturnType(desc);
        final int retSize = returnType.getSize();
        switch (opcode) {
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEINTERFACE:
                final List<Set<T>> argTaint = new ArrayList<>(argTypes.length);
                for (int i = 0; i < argTypes.length; i++) {
                    argTaint.add(null);
                }
                // 自栈顶向下 pop：单 slot 直接 pop；
                // long/double 双 slot 时把"高位"也合并到 argTaint，避免污点丢失。
                for (int i = 0; i < argTypes.length; i++) {
                    Type argType = argTypes[i];
                    if (argType.getSize() > 0) {
                        Set<T> merged = new HashSet<>();
                        for (int j = 0; j < argType.getSize() - 1; j++) {
                            merged.addAll(operandStack.pop());   // high slot 也算污点
                        }
                        merged.addAll(operandStack.pop());       // low slot 是真正语义的所在
                        argTaint.set(argTypes.length - 1 - i, merged);
                    }
                }
                if (retSize > 0) {
                    // 返回值默认无污点；具体的污点传播交给子类（TaintMethodAdapter）覆盖。
                    operandStack.push();
                    for (int i = 1; i < retSize; i++) {
                        operandStack.push();
                    }
                }
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        sanityCheck();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        int argsSize = 0;
        for (Type type : Type.getArgumentTypes(desc)) {
            argsSize += type.getSize();
        }
        int retSize = Type.getReturnType(desc).getSize();
        for (int i = 0; i < argsSize; i++) {
            operandStack.pop();
        }
        for (int i = 0; i < retSize; i++) {
            operandStack.push();
        }
        super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        sanityCheck();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        switch (opcode) {
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                operandStack.pop();
                break;
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
                operandStack.pop();
                operandStack.pop();
                break;
            case Opcodes.GOTO:
                break;
            case Opcodes.JSR:
                operandStack.push();
                super.visitJumpInsn(opcode, label);
                return;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        mergeGotoState(label);
        super.visitJumpInsn(opcode, label);
        sanityCheck();
    }

    @Override
    public void visitLabel(Label label) {
        // 1) 异常 handler 入口：JVM 规范要求此处栈应当被重置为"仅一个异常对象 ref"。
        //    必须在与 gotoState 合并之前先做，避免叠加到旧栈造成 sanityCheck 失败。
        boolean isHandler = exceptionHandlerLabels.contains(label);

        // 2) 取出此 label 已收集到的"前驱（GOTO/IF/SWITCH 等）状态"。
        GotoState<T> incoming = gotoStates.get(label);

        if (isHandler) {
            // 异常 handler：清空当前栈，仅 push 一个异常 ref（污点为前驱合并值，如有）
            operandStack.clear();
            Set<T> exTaint = new HashSet<>();
            if (incoming != null) {
                // handler 通常不会被 GOTO 直接命中，但若有，则保守取栈顶第一个槽
                OperandStack<T> os = incoming.getOperandStack();
                if (os.size() > 0) {
                    exTaint.addAll(os.get(0));
                }
                // locals 从前驱合并（保守）
                LocalVariables<T> lv = incoming.getLocalVariables();
                LocalVariables<T> mergedLv = new LocalVariables<>();
                for (Set<T> s : lv.getList()) {
                    mergedLv.add(new HashSet<>(s));
                }
                // 合并当前 locals
                for (int i = 0; i < localVariables.size(); i++) {
                    while (mergedLv.size() <= i) mergedLv.add(new HashSet<>());
                    mergedLv.get(i).addAll(localVariables.get(i));
                }
                this.localVariables = mergedLv;
            }
            operandStack.push(exTaint);
        } else if (incoming != null) {
            // 普通 label：合并"前驱状态" 与 "fall-through 进入时的当前状态"。
            // 旧实现是直接替换，会把 fall-through 路径的状态丢掉；这里改为 union。
            LocalVariables<T> mergedLv = new LocalVariables<>();
            OperandStack<T> mergedOs = new OperandStack<>();

            // 起点：前驱状态（深拷贝）
            for (Set<T> s : incoming.getLocalVariables().getList()) {
                mergedLv.add(new HashSet<>(s));
            }
            for (Set<T> s : incoming.getOperandStack().getList()) {
                mergedOs.add(new HashSet<>(s));
            }

            // 合并：fall-through 进入时的当前 state
            for (int i = 0; i < localVariables.size(); i++) {
                while (mergedLv.size() <= i) mergedLv.add(new HashSet<>());
                mergedLv.get(i).addAll(localVariables.get(i));
            }
            // 栈合并：以"前驱栈深"为准（fall-through 栈深应当一致；
            // 若不一致，以前驱栈深为基准并把当前 state 的污点 union 上去）。
            for (int i = 0; i < operandStack.size() && i < mergedOs.size(); i++) {
                mergedOs.getList().get(i).addAll(operandStack.getList().get(i));
            }

            this.operandStack = mergedOs;
            this.localVariables = mergedLv;
        }
        // 否则（无前驱、非 handler）：fall-through 唯一流入路径，保持当前 state 即可。

        super.visitLabel(label);
        sanityCheck();
    }

    @Override
    public void visitLdcInsn(Object cst) {
        if (cst instanceof Long || cst instanceof Double) {
            operandStack.push();
            operandStack.push();
        } else {
            operandStack.push();
        }
        super.visitLdcInsn(cst);
        sanityCheck();
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment);
        sanityCheck();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        operandStack.pop();
        mergeGotoState(dflt);
        for (Label label : labels) {
            mergeGotoState(label);
        }
        super.visitTableSwitchInsn(min, max, dflt, labels);
        sanityCheck();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        operandStack.pop();
        mergeGotoState(dflt);
        for (Label label : labels) {
            mergeGotoState(label);
        }
        super.visitLookupSwitchInsn(dflt, keys, labels);
        sanityCheck();
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
        for (int i = 0; i < dims; i++) {
            operandStack.pop();
        }
        operandStack.push();
        super.visitMultiANewArrayInsn(desc, dims);
        sanityCheck();
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        exceptionHandlerLabels.add(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }
}
