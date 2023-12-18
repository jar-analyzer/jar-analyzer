package com.n1ar4.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class FilterKillClassVisitor extends ClassVisitor {
    public FilterKillClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        // Filter 接口的处理
        if (mv != null && name.equals("doFilter") &&
                descriptor.equals("(Ljavax/servlet/ServletRequest;" +
                        "Ljavax/servlet/ServletResponse;Ljavax/servlet/FilterChain;)V")) {
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/FilterChain",
                    "doFilter", "(Ljavax/servlet/ServletRequest;" +
                            "Ljavax/servlet/ServletResponse;)V", true);
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 4);
            mv.visitEnd();
            return mv;
        }
        // HttpServlet 子类
        if (mv != null && name.equals("doFilter") &&
                descriptor.equals("(Ljavax/servlet/http/HttpServletRequest;" +
                        "Ljavax/servlet/http/HttpServletResponse;Ljavax/servlet/FilterChain;)V")) {
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "javax/servlet/FilterChain", "doFilter",
                    "(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V", true);
            mv.visitInsn(RETURN);
            mv.visitMaxs(3, 4);
            mv.visitEnd();
            return mv;
        }
        return mv;
    }
}
