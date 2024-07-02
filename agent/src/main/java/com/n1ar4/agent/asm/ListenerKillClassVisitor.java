package com.n1ar4.agent.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.RETURN;

public class ListenerKillClassVisitor extends ClassVisitor {
    public ListenerKillClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (mv != null && (name.equals("requestDestroyed") || name.equals("requestInitialized")) &&
                descriptor.equals("(Ljavax/servlet/ServletRequestEvent;)V")) {
            mv.visitCode();
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 2);
            mv.visitEnd();
            return mv;
        }
        return mv;
    }
}
