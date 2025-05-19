/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.HashSet;

public class MethodCallMethodVisitor extends MethodVisitor {
    private final HashSet<MethodReference.Handle> calledMethods;

    public MethodCallMethodVisitor(final int api, final MethodVisitor mv,
                                   final String ownerClass, String name, String desc,
                                   HashMap<MethodReference.Handle,
                                           HashSet<MethodReference.Handle>> methodCalls) {
        super(api, mv);
        this.calledMethods = new HashSet<>();
        methodCalls.put(
                new MethodReference.Handle(new ClassReference.Handle(ownerClass), name, desc),
                calledMethods);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        calledMethods.add(
                new MethodReference.Handle(
                        new ClassReference.Handle(owner), opcode, name, desc));
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor,
                                       Handle bootstrapMethodHandle,
                                       Object... bootstrapMethodArguments) {
        for (Object bsmArg : bootstrapMethodArguments) {
            if (bsmArg instanceof Handle) {
                Handle handle = (Handle) bsmArg;
                calledMethods.add(new MethodReference.Handle(
                        new ClassReference.Handle(handle.getOwner()),
                        handle.getName(), handle.getDesc()));
            }
        }
        super.visitInvokeDynamicInsn(name, descriptor,
                bootstrapMethodHandle, bootstrapMethodArguments);
    }
}
