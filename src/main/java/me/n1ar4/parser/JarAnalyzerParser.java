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
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.util.List;

public class JarAnalyzerParser {
    public static CompilationUnit buildInstance(String code) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        StaticJavaParser.getParserConfiguration().setSymbolResolver(symbolSolver);
        return StaticJavaParser.parse(code);
    }

    private static List<MethodDeclaration> getAllMethods(CompilationUnit cu) {
        return cu.findAll(MethodDeclaration.class);
    }

    public static List<ConstructorDeclaration> getAllConstructors(CompilationUnit cu) {
        return cu.findAll(ConstructorDeclaration.class);
    }

    public static InitializerDeclaration getStaticInitializerDeclaration(CompilationUnit cu) {
        List<InitializerDeclaration> ids = cu.findAll(InitializerDeclaration.class);
        for (InitializerDeclaration i : ids) {
            if (i.isStatic()) {
                return i;
            }
        }
        return null;
    }

    public static ConstructorDeclaration getConstructor(CompilationUnit cu, String desc) {
        List<ConstructorDeclaration> constructors = getAllConstructors(cu);
        DescInfo di = DescUtil.parseDesc(desc);
        for (ConstructorDeclaration constructor : constructors) {
            List<String> typeList = di.getParams();
            NodeList<Parameter> np = constructor.getParameters();
            if (np.size() != typeList.size()) {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < np.size(); i++) {
                String a = typeList.get(i);
                a = DescUtil.cleanJavaLang(a);
                String b = np.get(i).getTypeAsString();
                b = DescUtil.cleanJavaLang(b);
                if (!a.equals(b)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return constructor;
            }
        }
        return null;
    }

    public static MethodDeclaration getMethod(CompilationUnit cu, String name, String desc) {
        List<MethodDeclaration> list = getAllMethods(cu);
        DescInfo di = DescUtil.parseDesc(desc);
        for (MethodDeclaration method : list) {
            boolean match = true;
            if (method.getNameAsString().equals(name)) {
                List<String> typeList = di.getParams();
                NodeList<Parameter> np = method.getParameters();
                if (np.size() != typeList.size()) {
                    continue;
                }
                for (int i = 0; i < np.size(); i++) {
                    String a = typeList.get(i);
                    a = DescUtil.cleanJavaLang(a);
                    String b = np.get(i).getTypeAsString();
                    b = DescUtil.cleanJavaLang(b);
                    if (!a.equals(b)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    String c = di.getRet();
                    c = DescUtil.cleanJavaLang(c);
                    String d = method.getType().asString();
                    d = DescUtil.cleanJavaLang(d);
                    if (c.equals(d)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    public static Position findMethodCall(MethodDeclaration md, String method) {
        List<MethodCallExpr> mList = md.findAll(MethodCallExpr.class);
        for (MethodCallExpr mce : mList) {
            if (mce.getNameAsString().equals(method)) {
                if (mce.getBegin().isPresent()) {
                    return mce.getBegin().get();
                }
            }
        }
        return null;
    }
}
