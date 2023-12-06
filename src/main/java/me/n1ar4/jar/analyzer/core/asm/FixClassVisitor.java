package me.n1ar4.jar.analyzer.core.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class FixClassVisitor extends ClassVisitor {
    private String name;

    public String getName() {
        return name;
    }

    public FixClassVisitor() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        this.name = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
}
