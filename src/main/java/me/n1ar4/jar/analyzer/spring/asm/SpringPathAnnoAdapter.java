package me.n1ar4.jar.analyzer.spring.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

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
        return new ArrayVisitor(Opcodes.ASM6, av, results);
    }

    static class ArrayVisitor extends AnnotationVisitor {
        private final List<String> results;

        public ArrayVisitor(int api, AnnotationVisitor annotationVisitor, List<String> results) {
            super(api, annotationVisitor);
            this.results = results;
        }

        @Override
        public void visit(String name, Object value) {
            if (!value.toString().trim().equals("")) {
                results.add(value.toString());
            }
            super.visit(name, value);
        }
    }
}
