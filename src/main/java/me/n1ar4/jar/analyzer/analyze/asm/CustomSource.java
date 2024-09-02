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
