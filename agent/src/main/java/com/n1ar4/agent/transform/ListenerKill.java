package com.n1ar4.agent.transform;

import com.n1ar4.agent.asm.ListenerKillClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ListenerKill implements ClassFileTransformer {
    private final String className;

    public ListenerKill(String className) {
        this.className = className;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className, Class<?> clsMemShell,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        try {
            className = className.replace("/", ".");
            if (className.equals(this.className)) {
                ClassReader cr = new ClassReader(classfileBuffer);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                int api = Opcodes.ASM9;
                ClassVisitor cv = new ListenerKillClassVisitor(api, cw);
                int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
                cr.accept(cv, parsingOptions);
                return cw.toByteArray();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new byte[0];
    }
}
