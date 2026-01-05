/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.core.asm.JavaWebClassVisitor;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.Set;

public class OtherWebService {
    private static final Logger logger = LogManager.getLogger();

    public static void start(
            Set<ClassFileEntity> classFileList,
            ArrayList<String> interceptors,
            ArrayList<String> servlets,
            ArrayList<String> filters,
            ArrayList<String> listeners) {
        for (ClassFileEntity file : classFileList) {
            try {
                JavaWebClassVisitor jcv = new JavaWebClassVisitor(interceptors, servlets, filters, listeners);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(jcv, Const.AnalyzeASMOptions);
            } catch (Exception e) {
                logger.error("error: {}", e.getMessage());
            }
        }
    }
}
