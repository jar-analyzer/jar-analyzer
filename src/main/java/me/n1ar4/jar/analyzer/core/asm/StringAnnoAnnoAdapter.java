/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.AnnotationVisitor;

import java.util.*;

public class StringAnnoAnnoAdapter extends AnnotationVisitor {
    private final Set<String> internalData = new HashSet<>();
    private final Map<MethodReference.Handle, List<String>> stringAnnoMap;
    private final MethodReference.Handle mh;

    public StringAnnoAnnoAdapter(AnnotationVisitor annotationVisitor,
                                 MethodReference.Handle mh,
                                 Map<MethodReference.Handle, List<String>> stringAnnoMap) {
        super(Const.ASMVersion, annotationVisitor);
        this.mh = mh;
        this.stringAnnoMap = stringAnnoMap;
    }

    public List<String> getResults() {
        if (internalData.isEmpty()) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(internalData);
        }
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationVisitor av = super.visitArray(name);
        return new ArrayVisitor(Const.ASMVersion, av, internalData);
    }

    @Override
    public void visit(String name, Object value) {
        super.visit(name, value);
        this.internalData.add(value.toString());
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        super.visitEnum(name, descriptor, value);
        this.internalData.add(value);
    }

    static class ArrayVisitor extends AnnotationVisitor {
        private final Set<String> internalData;

        public ArrayVisitor(int api, AnnotationVisitor annotationVisitor, Set<String> results) {
            super(api, annotationVisitor);
            this.internalData = results;
        }

        @Override
        public void visit(String name, Object value) {
            if (!value.toString().trim().isEmpty()) {
                internalData.add(value.toString());
            }
            super.visit(name, value);
        }
    }

    @Override
    public void visitEnd() {
        List<String> data = getResults();
        if (!data.isEmpty()) {
            List<String> existData = this.stringAnnoMap.get(mh);
            if (existData != null) {
                existData.addAll(data);
            } else {
                this.stringAnnoMap.put(this.mh, data);
            }
        }
        super.visitEnd();
    }
}
