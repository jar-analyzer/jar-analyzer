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

import me.n1ar4.jar.analyzer.analyze.spring.SpringConstant;
import me.n1ar4.jar.analyzer.analyze.spring.SpringController;
import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

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
    private SpringPathAnnoAdapter pathAnnoAdapter = null;

    public SpringClassVisitor(List<SpringController> controllers,
                              Map<ClassReference.Handle, ClassReference> classMap,
                              Map<MethodReference.Handle, MethodReference> methodMap) {
        super(Const.ASMVersion);
        this.methodMap = methodMap;
        this.controllers = controllers;
        this.classMap = classMap;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationVisitor av = super.visitAnnotation(descriptor, visible);
        if (descriptor.equals(SpringConstant.RequestMappingAnno)) {
            this.pathAnnoAdapter = new SpringPathAnnoAdapter(Const.ASMVersion, av);
            return this.pathAnnoAdapter;
        }
        return av;
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
            if (pathAnnoAdapter != null) {
                if (!pathAnnoAdapter.getResults().isEmpty()) {
                    currentController.setBasePath(pathAnnoAdapter.getResults().get(0));
                }
            }
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
            if (pathAnnoAdapter != null) {
                if (!pathAnnoAdapter.getResults().isEmpty()) {
                    currentController.setBasePath(pathAnnoAdapter.getResults().get(0));
                }
            }
            return new SpringMethodAdapter(name, descriptor, this.name, Const.ASMVersion, mv,
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
