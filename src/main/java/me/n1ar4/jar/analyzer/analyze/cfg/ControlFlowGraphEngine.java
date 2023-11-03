package me.n1ar4.jar.analyzer.analyze.cfg;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.io.InputStream;
import java.util.Objects;

public class ControlFlowGraphEngine {
    public static void start(InputStream is, String methodName, String methodDesc,StringBuilder builder) throws Exception {
        ClassReader cr = new ClassReader(is);
        ClassNode cn = new ClassNode();
        int parsingOptions = ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
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
