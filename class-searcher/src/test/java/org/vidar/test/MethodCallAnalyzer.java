package org.vidar.test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

public class MethodCallAnalyzer {
    private Set<String> callingMethods;

    public MethodCallAnalyzer() {
        callingMethods = new HashSet<>();
    }

    public void analyzeClass(String className) {
        try {
            ClassReader classReader = new ClassReader(className);
            classReader.accept(new ClassVisitor(Opcodes.ASM6) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                    String currentMethodName = name;
                    System.out.println("当前解析到的方法是" + name);
                    System.out.println("下面输出其call的方法");
                    return new MethodVisitor(Opcodes.ASM6, methodVisitor) {

                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            System.out.println(owner + " " + name + " " + descriptor);
                            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                        }
                    };
                }
            }, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<String> getCallingMethods() {
        return callingMethods;
    }

    public static void main(String[] args) {
        MethodCallAnalyzer analyzer = new MethodCallAnalyzer();
        analyzer.analyzeClass("org/vidar/utils/YamlUtil");

//        Set<String> callingMethods = analyzer.getCallingMethods();
//        for (String method : callingMethods) {
//            System.out.println(method);
//        }
    }
}
