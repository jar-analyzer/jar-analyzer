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

package me.n1ar4.jar.analyzer.analyze.spring.asm;

import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.AnnotationVisitor;

import java.util.ArrayList;
import java.util.List;

public class SpringPathAnnoAdapter extends AnnotationVisitor {
    private final List<String> results = new ArrayList<>();

    public SpringPathAnnoAdapter(int api, AnnotationVisitor annotationVisitor) {
        super(api, annotationVisitor);
    }

    public List<String> getResults() {
        return results;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationVisitor av = super.visitArray(name);
        return new ArrayVisitor(Const.ASMVersion, av, results);
    }

    static class ArrayVisitor extends AnnotationVisitor {
        private final List<String> results;

        public ArrayVisitor(int api, AnnotationVisitor annotationVisitor, List<String> results) {
            super(api, annotationVisitor);
            this.results = results;
        }

        @Override
        public void visit(String name, Object value) {
            if (!value.toString().trim().isEmpty()) {
                results.add(value.toString());
            }
            super.visit(name, value);
        }
    }
}
