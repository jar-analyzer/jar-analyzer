package com.n1ar4.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ValveKillClassVisitor extends ClassVisitor {
    private String owner;

    public ValveKillClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        name = name.replace(".", "/");
        this.owner = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        // Valve invoke
        if (mv != null && name.equals("invoke") &&
                descriptor.equals("(Lorg/apache/catalina/connector/Request;" +
                        "Lorg/apache/catalina/connector/Response;)V")) {
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, owner,
                    "getNext", "()Lorg/apache/catalina/Valve;", false);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/apache/catalina/Valve",
                    "invoke", "(Lorg/apache/catalina/connector/Request;" +
                            "Lorg/apache/catalina/connector/Response;)V", true);
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
            return mv;
        }
        return mv;
    }
}
