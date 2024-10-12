/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.n1ar4.jar.analyzer.core;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import me.n1ar4.parser.JarAnalyzerParser;

public class FinderRunner {
    public static int find(String total, String methodName, String methodDesc) {
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
