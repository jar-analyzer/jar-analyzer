/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;


import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.Set;


public class CallGraphMethodAdapter extends CoreMethodAdapter<String> {
    private final Set<CallGraph> discoveredCalls;
    private final String owner;
    private final int access;
    private final String name;
    private final String desc;

    public CallGraphMethodAdapter(final int api, Set<CallGraph> discoveredCalls,
                                  final MethodVisitor mv, final String owner,
                                  int access, String name, String desc) {
        super(api, mv, owner, access, name, desc);
        this.owner = owner;
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.discoveredCalls = discoveredCalls;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        int localIndex = 0;
        int argIndex = 0;
        if ((this.access & Opcodes.ACC_STATIC) == 0) {
            localVariables.set(localIndex, "arg" + argIndex);
            localIndex += 1;
            argIndex += 1;
        }
        for (Type argType : Type.getArgumentTypes(desc)) {
            localVariables.set(localIndex, "arg" + argIndex);
            localIndex += argType.getSize();
            argIndex += 1;
        }
    }

    @Override
    @SuppressWarnings("all")
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        Type[] argTypes = Type.getArgumentTypes(desc);
        if (opcode != Opcodes.INVOKESTATIC) {
            Type[] extendedArgTypes = new Type[argTypes.length + 1];
            System.arraycopy(argTypes, 0, extendedArgTypes, 1, argTypes.length);
            extendedArgTypes[0] = Type.getObjectType(owner);
            argTypes = extendedArgTypes;
        }
        switch (opcode) {
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEINTERFACE:
                if (owner.equals("java/lang/String") && name.equals("valueOf")) {
                    Set<String> t = operandStack.get(0);
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    operandStack.set(0, t);
                    return;
                }
                if (owner.equals("java/lang/StringBuilder") && name.equals("append")) {
                    Set<String> t1 = operandStack.get(0);
                    Set<String> t2 = operandStack.get(1);
                    Set<String> t3 = new HashSet<>();
                    t3.addAll(t1);
                    t3.addAll(t2);
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    operandStack.get(0).addAll(t3);
                    return;
                }
                if (owner.equals("java/lang/StringBuilder") && name.equals("toString")) {
                    Set<String> t = operandStack.get(0);
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    operandStack.set(0, t);
                    return;
                }
                int stackIndex = 0;
                for (int i = 0; i < argTypes.length; i++) {
                    int argIndex = argTypes.length - 1 - i;
                    Type type = argTypes[argIndex];
                    Set<String> taint = operandStack.get(stackIndex);
                    if (taint.size() > 0) {
                        for (String argSrc : taint) {
                            if (!argSrc.startsWith("arg")) {
                                throw new IllegalStateException("invalid taint arg: " + argSrc);
                            }
                            int dotIndex = argSrc.indexOf('.');
                            int srcArgIndex;
                            if (dotIndex == -1) {
                                srcArgIndex = Integer.parseInt(argSrc.substring(3));
                            } else {
                                srcArgIndex = Integer.parseInt(argSrc.substring(3, dotIndex));
                            }
                            discoveredCalls.add(new CallGraph(
                                    new MethodReference.Handle(
                                            new ClassReference.Handle(this.owner), this.name, this.desc),
                                    new MethodReference.Handle(
                                            new ClassReference.Handle(owner), name, desc),
                                    srcArgIndex,
                                    argIndex));
                        }
                    }
                    stackIndex += type.getSize();
                }
                break;
            default:
                throw new IllegalStateException("unsupported opcode: " + opcode);
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
