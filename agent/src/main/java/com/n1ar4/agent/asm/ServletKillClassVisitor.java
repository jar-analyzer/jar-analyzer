package com.n1ar4.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.RETURN;

public class ServletKillClassVisitor extends ClassVisitor {
    public ServletKillClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        // Servlet service
        if (mv != null && name.equals("service") &&
                descriptor.equals("(Ljavax/servlet/ServletRequest;Ljavax/servlet/ServletResponse;)V")) {
            mv.visitCode();
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 3);
            mv.visitEnd();
            return mv;
        }
        // HttpServlet doAny
        if (mv != null && (name.equals("doGet") || name.equals("doPost")
                || name.equals("doDelete") || name.equals("doHead") || name.equals("doOptions")
                || name.equals("doPut") || name.equals("doTrace")) &&
                descriptor.equals("(Ljavax/servlet/http/HttpServletRequest;" +
                        "Ljavax/servlet/http/HttpServletResponse;)V")) {
            mv.visitCode();
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 3);
            mv.visitEnd();
        }
        return mv;
    }
}
