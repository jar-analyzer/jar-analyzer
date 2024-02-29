package me.n1ar4.rasp.agent.asm;

import me.n1ar4.rasp.agent.ent.HookInfo;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class OisMethodAdapter extends BaseMethodAdapter {
    public OisMethodAdapter(int api, MethodVisitor mv, HookInfo hook, String desc) {
        super(api, mv, hook, desc);
    }

    @Override
    public void visitCode() {
        super.visitCode();

        int stacksIndex = this.paramsNum + 1;

        invokeStack();
        super.visitVarInsn(ASTORE, stacksIndex);
        // type
        super.visitInsn(ICONST_3);
        super.visitVarInsn(ALOAD, stacksIndex);
        // 0 -> this
        super.visitVarInsn(ALOAD, 0);
        super.visitMethodInsn(INVOKESTATIC, "me/n1ar4/rasp/agent/core/Upload", "uploadObject",
                "(I[Ljava/lang/StackTraceElement;Ljava/lang/Object;)V", false);
        resolveBlock();
    }
}
