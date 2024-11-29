/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.analyze.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;

import java.util.*;

public class CustomSource extends SourceInterpreter {
    private final Map<AbstractInsnNode, Set<SourceValue>> sources;

    public CustomSource(Map<AbstractInsnNode, Set<SourceValue>> sources) {
        super(ASM9);
        this.sources = sources;
    }

    @Override
    public SourceValue unaryOperation(AbstractInsnNode insn, SourceValue value) {
        sources.computeIfAbsent(insn, x -> new HashSet<>()).add(value);
        return super.unaryOperation(insn, value);
    }

    @Override
    public SourceValue binaryOperation(AbstractInsnNode insn, SourceValue v1, SourceValue v2) {
        addAll(insn, Arrays.asList(v1, v2));
        return super.binaryOperation(insn, v1, v2);
    }

    @Override
    public SourceValue ternaryOperation(AbstractInsnNode insn, SourceValue v1, SourceValue v2, SourceValue v3) {
        addAll(insn, Arrays.asList(v1, v2, v3));
        return super.ternaryOperation(insn, v1, v2, v3);
    }

    @Override
    public SourceValue naryOperation(AbstractInsnNode insn, List<? extends SourceValue> values) {
        addAll(insn, values);
        return super.naryOperation(insn, values);
    }

    private void addAll(AbstractInsnNode insn, List<? extends SourceValue> values) {
        sources.computeIfAbsent(insn, x -> new HashSet<>()).addAll(values);
    }

}
