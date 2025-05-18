/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.core.reference.AnnoReference;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.Set;

public class DiscoveryMethodAdapter extends MethodVisitor {

    private final Set<AnnoReference> anno;

    private final MethodReference methodReference;

    protected DiscoveryMethodAdapter(int api, MethodVisitor methodVisitor,
                                     Set<AnnoReference> anno, MethodReference methodReference) {
        super(api, methodVisitor);
        this.anno = anno;
        this.methodReference = methodReference;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnoReference annoReference = new AnnoReference();
        annoReference.setAnnoName(descriptor);
        annoReference.setVisible(visible);
        anno.add(annoReference);
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        AnnoReference annoReference = new AnnoReference();
        annoReference.setAnnoName(descriptor);
        annoReference.setVisible(visible);
        annoReference.setParameter(parameter);
        anno.add(annoReference);
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
