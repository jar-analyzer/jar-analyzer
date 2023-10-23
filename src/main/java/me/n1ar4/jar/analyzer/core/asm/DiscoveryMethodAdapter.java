package me.n1ar4.jar.analyzer.core.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.Set;

public class DiscoveryMethodAdapter extends MethodVisitor {

    private final Set<String> anno;

    protected DiscoveryMethodAdapter(int api, MethodVisitor methodVisitor,
                                     Set<String> anno) {
        super(api, methodVisitor);
        this.anno = anno;
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
}
