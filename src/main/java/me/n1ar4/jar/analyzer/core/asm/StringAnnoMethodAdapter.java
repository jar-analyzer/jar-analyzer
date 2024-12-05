package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

import java.util.List;
import java.util.Map;

public class StringAnnoMethodAdapter extends MethodVisitor {
    private final Map<MethodReference.Handle, List<String>> stringAnnoMap;
    private final MethodReference.Handle mh;

    protected StringAnnoMethodAdapter(MethodVisitor methodVisitor,
                                      MethodReference.Handle mh,
                                      Map<MethodReference.Handle, List<String>> stringAnnoMap) {
        super(Const.ASMVersion, methodVisitor);
        this.mh = mh;
        this.stringAnnoMap = stringAnnoMap;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
        return new StringAnnoAnnoAdapter(av,mh,stringAnnoMap);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        return new StringAnnoAnnoAdapter(av, mh, stringAnnoMap);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        return new StringAnnoAnnoAdapter(av, mh, stringAnnoMap);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitParameterAnnotation(parameter, descriptor, visible);
        return new StringAnnoAnnoAdapter(av, mh, stringAnnoMap);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
        return new StringAnnoAnnoAdapter(av, mh, stringAnnoMap);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
        return new StringAnnoAnnoAdapter(av, mh, stringAnnoMap);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        AnnotationVisitor av = super.visitAnnotationDefault();
        return new StringAnnoAnnoAdapter(av, mh, stringAnnoMap);
    }
}
