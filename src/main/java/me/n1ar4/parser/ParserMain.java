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

package me.n1ar4.parser;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

public class ParserMain {
    private static final String code = "package me.n1ar4.log;\n" +
            "\n" +
            "public class LogUtil {\n" +
            "    private static final int STACK_TRACE_INDEX = 5;\n" +
            "\n" +
            "    public static String getClassName() {\n" +
            "        String fullClassName = Thread.currentThread()\n" +
            "                .getStackTrace()[STACK_TRACE_INDEX].getClassName();\n" +
            "        int lastDotIndex = fullClassName.lastIndexOf('.');\n" +
            "        if (lastDotIndex != -1) {\n" +
            "            return fullClassName.substring(lastDotIndex + 1);\n" +
            "        } else {\n" +
            "            return fullClassName;\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public static String getMethodName() {\n" +
            "        return Thread.currentThread()\n" +
            "                .getStackTrace()[STACK_TRACE_INDEX].getMethodName();\n" +
            "    }\n" +
            "\n" +
            "    public static String getLineNumber() {\n" +
            "        return String.valueOf(Thread.currentThread()\n" +
            "                .getStackTrace()[STACK_TRACE_INDEX].getLineNumber());\n" +
            "    }\n" +
            "}";

    public static void main(String[] args) {
//        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
//        combinedTypeSolver.add(new ReflectionTypeSolver());
//        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
//        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
//        CompilationUnit cu = StaticJavaParser.parse(code);
//
//
//        cu.findAll(MethodDeclaration.class).stream()
//                .filter(m -> m.getNameAsString().equals("getClassName"))
//                .forEach(m -> {
//                    m.findAll(MethodCallExpr.class).stream()
//                            .filter(mc -> mc.getNameAsString().equals("lastIndexOf"))
//                            .forEach(mc -> {
//                                System.out.println(mc);
//                            });
//                });
        CompilationUnit cu = JarAnalyzerParser.buildInstance(code);
        MethodDeclaration md = JarAnalyzerParser.getMethod(cu, "getClassName", "()Ljava/lang/String;");
        Position p = JarAnalyzerParser.findMethodCall(md, "lastIndexOf");
        System.out.println(p);
    }
}
