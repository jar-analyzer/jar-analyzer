package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.core.asm.DiscoveryClassVisitor;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;

import java.util.Map;
import java.util.Set;

public class DiscoveryRunner {
    private static final Logger logger = LogManager.getLogger();

    public static void start(Set<ClassFileEntity> classFileList,
                             Set<ClassReference> discoveredClasses,
                             Set<MethodReference> discoveredMethods,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, MethodReference> methodMap) {
        logger.info("start class analyze");
        for (ClassFileEntity file : classFileList) {
            try {
                DiscoveryClassVisitor dcv = new DiscoveryClassVisitor(discoveredClasses,
                        discoveredMethods, file.getJarName());
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(dcv, Const.AnalyzeASMOptions);
            } catch (Exception e) {
                logger.error("discovery error: {}", e.toString());
            }
        }
        for (ClassReference clazz : discoveredClasses) {
            classMap.put(clazz.getHandle(), clazz);
        }
        for (MethodReference method : discoveredMethods) {
            methodMap.put(method.getHandle(), method);
        }
    }
}
