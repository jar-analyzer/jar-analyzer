package me.n1ar4.jar.analyzer.analyze.spring.asm;

import me.n1ar4.jar.analyzer.analyze.spring.SpringConstant;
import me.n1ar4.jar.analyzer.analyze.spring.SpringController;
import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpringClassVisitor extends ClassVisitor {
    private final Map<ClassReference.Handle, ClassReference> classMap;
    private final Map<MethodReference.Handle, MethodReference> methodMap;
    private final List<SpringController> controllers;
    private boolean isSpring;
    private SpringController currentController;
    private String name;

    public SpringClassVisitor(List<SpringController> controllers,
                              Map<ClassReference.Handle, ClassReference> classMap,
                              Map<MethodReference.Handle, MethodReference> methodMap) {
        super(Opcodes.ASM9);
        this.methodMap = methodMap;
        this.controllers = controllers;
        this.classMap = classMap;
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        this.name = name;
        Set<String> annotations = classMap.get(new ClassReference.Handle(name)).getAnnotations();
        if (annotations.contains(SpringConstant.ControllerAnno) ||
                annotations.contains(SpringConstant.RestControllerAnno) ||
                annotations.contains(SpringConstant.SBApplication)) {
            this.isSpring = true;
            currentController = new SpringController();
            currentController.setClassReference(classMap.get(new ClassReference.Handle(name)));
            currentController.setClassName(new ClassReference.Handle(name));
            currentController.setRest(!annotations.contains(SpringConstant.ControllerAnno));
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (this.isSpring) {
            if (this.name.equals("<init>") || this.name.equals("<clinit>")) {
                return mv;
            }
            return new SpringMethodAdapter(name, descriptor, this.name, Opcodes.ASM9, mv,
                    currentController, this.methodMap);
        } else {
            return mv;
        }
    }

    @Override
    public void visitEnd() {
        if (isSpring) {
            controllers.add(currentController);
        }
        super.visitEnd();
    }
}
