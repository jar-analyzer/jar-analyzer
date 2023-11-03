package me.n1ar4.jar.analyzer.analyze.frame;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.io.InputStream;
import java.util.List;

public class FrameEngine {
    public static String start(InputStream is, String methodName, String methodDesc, StringBuilder builder) throws Exception {
        ClassReader cr = new ClassReader(is);
        ClassNode cn = new ClassNode();
        cr.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
        String owner = cn.name;
        List<MethodNode> methods = cn.methods;
        MethodNode method = null;
        for (MethodNode mn : methods) {
            if (mn.name.equals(methodName) && mn.desc.equals(methodDesc)) {
                method = mn;
            }
        }
        if (method != null) {
            return print(owner, method,builder);
        }
        return null;
    }

    private static String print(String owner, MethodNode mn,StringBuilder builder) throws AnalyzerException {
        Analyzer<BasicValue> analyzer = new Analyzer<>(new SimpleVerifier());
        FrameUtils.printGraph(owner, mn, analyzer, ValueUtils::fromBasicValue2String,builder);
        return null;
    }
}
