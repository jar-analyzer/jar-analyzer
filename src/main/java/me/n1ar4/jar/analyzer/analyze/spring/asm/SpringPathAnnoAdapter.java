/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
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
