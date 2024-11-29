/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
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
