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

import me.n1ar4.jar.analyzer.core.MethodReference;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Set;

public class DiscoveryMethodAdapter extends MethodVisitor {

    private final Set<String> anno;

    private final MethodReference methodReference;

    protected DiscoveryMethodAdapter(int api, MethodVisitor methodVisitor,
                                     Set<String> anno, MethodReference methodReference) {
        super(api, methodVisitor);
        this.anno = anno;
        this.methodReference = methodReference;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        anno.add(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        anno.add(descriptor);
        return super.visitParameterAnnotation(parameter, descriptor, visible);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        int lineNumber = methodReference.getLineNumber();
        if (lineNumber == -1) {
            this.methodReference.setLineNumber(line);
        }
        super.visitLineNumber(line, start);
    }
}
