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

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.HashSet;

public class MethodCallMethodVisitor extends MethodVisitor {
    private final HashSet<MethodReference.Handle> calledMethods;

    public MethodCallMethodVisitor(final int api, final MethodVisitor mv,
                                   final String owner, String name, String desc,
                                   HashMap<MethodReference.Handle,
                                           HashSet<MethodReference.Handle>> methodCalls) {
        super(api, mv);
        this.calledMethods = new HashSet<>();
        methodCalls.put(
                new MethodReference.Handle(
                        new ClassReference.Handle(owner), name, desc), calledMethods);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        calledMethods.add(
                new MethodReference.Handle(
                        new ClassReference.Handle(owner), name, desc));
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor,
                                       Handle bootstrapMethodHandle,
                                       Object... bootstrapMethodArguments) {
        for (Object bsmArg : bootstrapMethodArguments) {
            if (bsmArg instanceof Handle) {
                Handle handle = (Handle) bsmArg;
                calledMethods.add(new MethodReference.Handle(
                        new ClassReference.Handle(handle.getOwner()),
                        handle.getName(), handle.getDesc()));
            }
        }
        super.visitInvokeDynamicInsn(name, descriptor,
                bootstrapMethodHandle, bootstrapMethodArguments);
    }
}
