package me.n1ar4.rasp.agent.asm;

import me.n1ar4.rasp.agent.ent.HookInfo;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;


public class ProcessClassVisitor extends BaseClassVisitor {
    public ProcessClassVisitor(int api, ClassVisitor classVisitor,String name) {
        super(api, classVisitor,name);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        for (HookInfo h : this.hooks) {
            String hName = h.getMethodName();
            String hDesc = h.getMethodDesc();
            if(!h.getClassName().equals(targetClassName)){
                continue;
            }
            if (hName.equals(name) && hDesc.equals(descriptor)) {
                return new ProcessMethodAdapter(api, mv, h, descriptor);
            }
        }
        return mv;
    }
}
