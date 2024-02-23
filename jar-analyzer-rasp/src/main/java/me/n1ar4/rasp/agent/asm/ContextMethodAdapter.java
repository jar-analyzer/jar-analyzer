package me.n1ar4.rasp.agent.asm;

import me.n1ar4.rasp.agent.ent.HookInfo;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ContextMethodAdapter extends BaseMethodAdapter {
    public ContextMethodAdapter(int api, MethodVisitor mv, HookInfo hook, String desc) {
        super(api, mv, hook, desc);
    }

    @Override
    public void visitCode() {
        super.visitCode();

        int stacksIndex = this.paramsNum + 1;

        invokeStack();

        super.visitVarInsn(ASTORE, stacksIndex);
        // type
        super.visitInsn(ICONST_2);
        super.visitVarInsn(ALOAD, stacksIndex);
        // 0 -> this
        // 1 -> String param
        super.visitVarInsn(ALOAD, 1);
        super.visitMethodInsn(INVOKESTATIC, "me/n1ar4/rasp/agent/core/Upload", "uploadString",
                "(I[Ljava/lang/StackTraceElement;Ljava/lang/String;)V", false);
        resolveBlock();
    }
}
