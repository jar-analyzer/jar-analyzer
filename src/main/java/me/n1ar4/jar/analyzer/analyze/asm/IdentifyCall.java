package me.n1ar4.jar.analyzer.analyze.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.*;

public class IdentifyCall {
    private final InsnList instructions;
    private final Map<AbstractInsnNode, Set<SourceValue>> sources;
    private final TreeMap<int[], AbstractInsnNode> conditionals;

    private IdentifyCall(InsnList il,
                         Map<AbstractInsnNode, Set<SourceValue>> s, TreeMap<int[], AbstractInsnNode> c) {
        instructions = il;
        sources = s;
        conditionals = c;
    }

    Set<AbstractInsnNode> getAllInputsOf(AbstractInsnNode instr) {
        Set<AbstractInsnNode> source = new HashSet<>();
        List<SourceValue> pending = new ArrayList<>(sources.get(instr));
        for (int pIx = 0; pIx < pending.size(); pIx++) {
            SourceValue sv = pending.get(pIx);
            final boolean branch = sv.insns.size() > 1;
            for (AbstractInsnNode in : sv.insns) {
                if (source.add(in))
                    pending.addAll(sources.getOrDefault(in, Collections.emptySet()));
                if (branch) {
                    int ix = instructions.indexOf(in);
                    conditionals.forEach((b, i) -> {
                        if (b[0] <= ix && b[1] >= ix && source.add(i))
                            pending.addAll(sources.getOrDefault(i, Collections.emptySet()));
                    });
                }
            }
        }
        return source;
    }

    static IdentifyCall getInputs(
            String internalClassName, MethodNode toAnalyze) throws AnalyzerException {

        InsnList instructions = toAnalyze.instructions;
        Map<AbstractInsnNode, Set<SourceValue>> sources = new HashMap<>();
        SourceInterpreter i = new CustomSource(sources);
        TreeMap<int[], AbstractInsnNode> conditionals = new TreeMap<>(
                Comparator.comparingInt((int[] a) -> a[0]).thenComparingInt(a -> a[1]));
        Analyzer<SourceValue> analyzer = new Analyzer<SourceValue>(i) {
            @Override
            protected void newControlFlowEdge(int insn, int successor) {
                if (insn != successor - 1) {
                    AbstractInsnNode instruction = instructions.get(insn);
                    Set<SourceValue> dep = sources.get(instruction);
                    if (dep != null && !dep.isEmpty())
                        conditionals.put(new int[]{insn, successor}, instruction);
                }
            }
        };
        analyzer.analyze(internalClassName, toAnalyze);
        return new IdentifyCall(instructions, sources, conditionals);
    }
}