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

package me.n1ar4.jar.analyzer.analyze.cfg;

import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.io.InputStream;
import java.util.Objects;

public class ControlFlowGraphEngine {
    public static void start(InputStream is, String methodName, String methodDesc, StringBuilder builder) throws Exception {
        ClassReader cr = new ClassReader(is);
        ClassNode cn = new ClassNode();
        int parsingOptions = Const.GlobalASMOptions;
        cr.accept(cn, parsingOptions);
        MethodNode targetNode = null;
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(methodName) && mn.desc.equals(methodDesc)) {
                targetNode = mn;
                break;
            }
        }
        if (targetNode == null) {
            return;
        }
        builder.append(display(cn.name, targetNode));
    }

    private static String display(String owner, MethodNode mn) throws AnalyzerException {
        InsnBlock[] blocks;
        Objects.requireNonNull(ControlFlowGraphType.STANDARD);
        CFGAnalyzer<BasicValue> analyzer = new CoreCFGAnalyzer<>(new BasicInterpreter());
        analyzer.analyze(owner, mn);
        blocks = analyzer.getBlocks();
        TextGraph textGraph = new TextGraph(blocks);
        return textGraph.draw(0, 0);
    }
}
