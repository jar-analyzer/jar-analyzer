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