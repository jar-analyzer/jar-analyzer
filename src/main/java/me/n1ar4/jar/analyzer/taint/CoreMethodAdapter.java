package me.n1ar4.jar.analyzer.taint;

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
public class CoreMethodAdapter<T> extends MethodVisitor {
    private final AnalyzerAdapter analyzerAdapter;
    private final int access;
    private final String desc;

    private final Map<Label, GotoState<T>> gotoStates = new HashMap<>();
    private final Set<Label> exceptionHandlerLabels = new HashSet<>();

    protected OperandStack<T> operandStack;
    protected LocalVariables<T> localVariables;

    public CoreMethodAdapter(final int api, final MethodVisitor mv, final String owner,
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
                while (newLocalVariables.size()<=i){
                    newLocalVariables.add(new HashSet<>());
                }
                newLocalVariables.get(i).addAll(localVariables.get(i));
            }
            for (int i = 0; i < operandStack.size(); i++) {
                while (newOperandStack.size()<=i){
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
                operandStack.push(operandStack.get(0));
                break;
            case Opcodes.DUP_X1:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                operandStack.push(saved0);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP_X2:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                operandStack.push(saved0);
                operandStack.push(saved2);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP2:
                operandStack.push(operandStack.get(1));
                operandStack.push(operandStack.get(1));
                break;
            case Opcodes.DUP2_X1:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                operandStack.push(saved1);
                operandStack.push(saved0);
                operandStack.push(saved2);
                operandStack.push(saved1);
                operandStack.push(saved0);
                break;
            case Opcodes.DUP2_X2:
                saved0 = operandStack.pop();
                saved1 = operandStack.pop();
                saved2 = operandStack.pop();
                saved3 = operandStack.pop();
                operandStack.push(saved1);
                operandStack.push(saved0);
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
        for (int i = localVariables.size(); i <= var; i++) {
            localVariables.add(new HashSet<>());
        }
        Set<T> saved0;
        switch (opcode) {
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
                operandStack.push(localVariables.get(var));
                break;
            case Opcodes.LLOAD:
            case Opcodes.DLOAD:
                operandStack.push();
                operandStack.push();
                break;
            case Opcodes.ALOAD:
                operandStack.push(localVariables.get(var));
                break;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
                localVariables.set(var, operandStack.pop());
                break;
            case Opcodes.DSTORE:
            case Opcodes.LSTORE:
                operandStack.pop();
                operandStack.pop();
                localVariables.set(var, new HashSet<>());
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
                for (int i = 0; i < argTypes.length; i++) {
                    Type argType = argTypes[i];
                    if (argType.getSize() > 0) {
                        for (int j = 0; j < argType.getSize() - 1; j++) {
                            operandStack.pop();
                        }
                        argTaint.set(argTypes.length - 1 - i, operandStack.pop());
                    }
                }
                Set<T> resultTaint;
                if (name.equals("<init>")) {
                    resultTaint = argTaint.get(0);
                } else {
                    resultTaint = new HashSet<>();
                }
                if (retSize > 0) {
                    operandStack.push(resultTaint);
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
            this.operandStack = newOperandStack;
            this.localVariables = newLocalVariables;
        }
        if (exceptionHandlerLabels.contains(label)) {
            operandStack.push(new HashSet<>());
        }
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

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
