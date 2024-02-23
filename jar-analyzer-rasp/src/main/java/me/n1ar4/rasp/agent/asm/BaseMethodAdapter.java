package me.n1ar4.rasp.agent.asm;

import me.n1ar4.rasp.agent.core.Configuration;
import me.n1ar4.rasp.agent.ent.HookInfo;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class BaseMethodAdapter extends MethodVisitor {
    protected final HookInfo h;
    protected final int paramsNum;

    public BaseMethodAdapter(int api, MethodVisitor mv,
                             HookInfo hook, String desc) {
        super(api, mv);
        this.h = hook;
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        this.paramsNum = argumentTypes.length;
    }

    protected void invokeStack() {
        super.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread",
                "()Ljava/lang/Thread;", false);
        super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace",
                "()[Ljava/lang/StackTraceElement;", false);
    }

    protected void resolveBlock() {
        if (Configuration.shouldBlock()) {
            super.visitTypeInsn(NEW, "java/lang/Exception");
            super.visitInsn(DUP);
            super.visitLdcInsn("jar-analyzer rasp hooked!");
            super.visitMethodInsn(INVOKESPECIAL, "java/lang/Exception", "<init>",
                    "(Ljava/lang/String;)V", false);
            super.visitInsn(ATHROW);
        } else {
            super.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                    "Ljava/io/PrintStream;");
            super.visitLdcInsn("jar-analyzer rasp hooked!");
            super.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
                    "(Ljava/lang/String;)V", false);
        }
    }
}
