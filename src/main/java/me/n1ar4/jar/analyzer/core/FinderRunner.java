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

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import me.n1ar4.parser.JarAnalyzerParser;

public class FinderRunner {
    private static final Logger logger = LogManager.getLogger();

    public static int find(String total, String methodName, String methodDesc) {
        try {
            // FIX <init> CLASS NAME
            CompilationUnit cu = JarAnalyzerParser.buildInstance(total);
            if (methodName.equals("<clinit>")) {
                InitializerDeclaration id = JarAnalyzerParser.getStaticInitializerDeclaration(cu);
                if (id == null) {
                    return 0;
                }
                if (id.getBegin().isPresent()) {
                    return getCur(total, id.getBegin().get());
                } else {
                    return 0;
                }
            }
            if (methodName.equals("<init>")) {
                ConstructorDeclaration cd = JarAnalyzerParser.getConstructor(cu, methodDesc);
                if (cd == null) {
                    return 0;
                }
                SimpleName sn = cd.getName();
                if (sn.getBegin().isPresent()) {
                    return getCur(total, sn.getBegin().get());
                } else {
                    return 0;
                }
            }
            MethodDeclaration md = JarAnalyzerParser.getMethod(cu, methodName, methodDesc);
            if (md == null) {
                return 0;
            }
            SimpleName sn = md.getName();
            if (sn.getBegin().isPresent()) {
                return getCur(total, sn.getBegin().get());
            } else {
                return 0;
            }
        } catch (Exception ex) {
            logger.debug("find method position bug: {}", ex.getMessage());
            return 0;
        }
    }

    public static int getCur(String code, Position position) {
        String[] lines = code.split("\n");
        int charCount = 0;
        for (int i = 0; i < position.line - 1; i++) {
            charCount += lines[i].length() + 1;
        }
        charCount += position.column;
        return charCount;
    }
}
