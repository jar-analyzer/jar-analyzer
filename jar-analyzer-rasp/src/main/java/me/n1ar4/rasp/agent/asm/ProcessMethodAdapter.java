package me.n1ar4.rasp.agent.asm;

import me.n1ar4.rasp.agent.ent.HookInfo;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class ProcessMethodAdapter extends BaseMethodAdapter {

    public ProcessMethodAdapter(int api, MethodVisitor mv, HookInfo hook, String desc) {
        super(api, mv, hook, desc);
    }

    @Override
    public void visitCode() {
        super.visitCode();

        // 注意不要占用原有局部变量表
        int stacksIndex = this.paramsNum + 1;

        invokeStack();

        super.visitVarInsn(ASTORE, stacksIndex);
        // type
        super.visitInsn(ICONST_1);
        // stack
        super.visitVarInsn(ALOAD, stacksIndex);
        // this
        super.visitVarInsn(ALOAD, 0);
        // this.command
        super.visitFieldInsn(GETFIELD, "java/lang/ProcessBuilder",
                "command", "Ljava/util/List;");
        // uploadStringList(type,stack,this.command)
        super.visitMethodInsn(INVOKESTATIC, "me/n1ar4/rasp/agent/core/Upload", "uploadStringList",
                "(I[Ljava/lang/StackTraceElement;Ljava/util/List;)V", false);

        resolveBlock();
    }
}
