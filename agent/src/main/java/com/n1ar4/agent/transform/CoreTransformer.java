package com.n1ar4.agent.transform;


import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;


public class CoreTransformer implements ClassFileTransformer {

    private final String targetClass;

    public byte[] data;

    public CoreTransformer(String targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        className = className.replace("/", ".");
        if (className.equals(targetClass)) {
            System.out.println("get bytecode form: " + className);
            data = new byte[classfileBuffer.length + 1];
            System.arraycopy(classfileBuffer, 0, data, 0, classfileBuffer.length);
            System.out.println("bytecode length: " + data.length);
        }
        return classfileBuffer;
    }
}