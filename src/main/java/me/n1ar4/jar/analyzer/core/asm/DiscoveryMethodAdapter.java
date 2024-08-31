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
