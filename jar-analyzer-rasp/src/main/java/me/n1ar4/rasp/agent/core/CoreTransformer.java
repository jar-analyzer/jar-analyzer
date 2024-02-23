package me.n1ar4.rasp.agent.core;

import me.n1ar4.rasp.agent.asm.ContextClassVisitor;
import me.n1ar4.rasp.agent.asm.ProcessClassVisitor;
import me.n1ar4.rasp.agent.ent.Const;
import me.n1ar4.rasp.agent.utils.Log;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

public class CoreTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        try {
            if (className.equals(Const.ProcessBuilder)) {
                return transformASM(className, ProcessClassVisitor.class);
            }
            if (className.equals(Const.InitialContext)) {
                return transformASM(className, ContextClassVisitor.class);
            }
        } catch (ClassCircularityError | Exception ex) {
            return classfileBuffer;
        }
        return classfileBuffer;
    }

    public byte[] transformASM(String className, Class<?> cvClass) {
        try {
            byte[] b = getBytes(className, cvClass);

            if (Configuration.isDebug()) {
                Path tempPath = Paths.get("temp");
                if (!Files.exists(tempPath)) {
                    Files.createDirectories(tempPath);
                }
                String[] csp = className.split("/");
                String fileName = String.format("%s%s%s.class",
                        tempPath, File.separator, csp[csp.length - 1]);
                Files.write(Paths.get(fileName), b);
            }

            return b;
        } catch (Exception ignored) {
        }
        return null;
    }

    private static byte[] getBytes(String className, Class<?> cvClass) throws Exception {
        ClassReader classReader = new ClassReader(className);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        Constructor<?> constructor = cvClass.getConstructor(
                int.class, ClassVisitor.class, String.class);
        ClassVisitor cv = (ClassVisitor) constructor.newInstance(
                Opcodes.ASM9, classWriter, className);

        classReader.accept(cv, ClassReader.EXPAND_FRAMES);
        return classWriter.toByteArray();
    }
}