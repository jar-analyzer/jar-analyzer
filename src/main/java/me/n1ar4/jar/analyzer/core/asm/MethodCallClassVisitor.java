/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.HashSet;

public class MethodCallClassVisitor extends ClassVisitor {
    private String ownerClass;

    private final HashMap<MethodReference.Handle, HashSet<MethodReference.Handle>> methodCalls;

    public MethodCallClassVisitor(HashMap<MethodReference.Handle,
            HashSet<MethodReference.Handle>> methodCalls) {
        super(Const.ASMVersion);
        this.methodCalls = methodCalls;
    }

    @Override
    public void visit(int version, int access, String ownerClass, String signature,
                      String superName, String[] interfaces) {
        super.visit(version, access, ownerClass, signature, superName, interfaces);
        this.ownerClass = ownerClass;
    }

    public MethodVisitor visitMethod(int access, String methodName, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, methodName, desc, signature, exceptions);
        return new MethodCallMethodVisitor(api, mv, this.ownerClass, methodName, desc, methodCalls);
    }
}
