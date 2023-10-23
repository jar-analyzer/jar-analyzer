package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.HashSet;

public class MethodCallMethodVisitor extends MethodVisitor {
    private final HashSet<MethodReference.Handle> calledMethods;

    public MethodCallMethodVisitor(final int api, final MethodVisitor mv,
                                   final String owner, String name, String desc,
                                   HashMap<MethodReference.Handle,
                                           HashSet<MethodReference.Handle>> methodCalls) {
        super(api, mv);
        this.calledMethods = new HashSet<>();
        methodCalls.put(
                new MethodReference.Handle(
                        new ClassReference.Handle(owner), name, desc), calledMethods);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        calledMethods.add(
                new MethodReference.Handle(
                        new ClassReference.Handle(owner), name, desc));
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
