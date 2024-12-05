/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.core.asm.DiscoveryClassVisitor;
import me.n1ar4.jar.analyzer.core.asm.StringAnnoClassVisitor;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DiscoveryRunner {
    private static final Logger logger = LogManager.getLogger();

    public static void start(Set<ClassFileEntity> classFileList,
                             Set<ClassReference> discoveredClasses,
                             Set<MethodReference> discoveredMethods,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, MethodReference> methodMap,
                             Map<MethodReference.Handle, List<String>> stringAnnoMap) {
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
        logger.info("start string annotation analyze");
        for (ClassFileEntity file : classFileList) {
            try {
                StringAnnoClassVisitor sav = new StringAnnoClassVisitor(stringAnnoMap);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(sav, Const.AnalyzeASMOptions);
            } catch (Exception e) {
                logger.error("discovery error: {}", e.toString());
            }
        }
    }
}
