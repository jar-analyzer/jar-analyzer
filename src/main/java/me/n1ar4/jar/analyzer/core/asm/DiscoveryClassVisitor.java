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

package me.n1ar4.jar.analyzer.core.asm;

import me.n1ar4.jar.analyzer.core.ClassReference;
import me.n1ar4.jar.analyzer.core.MethodReference;
import me.n1ar4.jar.analyzer.starter.Const;
import org.objectweb.asm.*;

import java.util.*;

public class DiscoveryClassVisitor extends ClassVisitor {
    private String name;
    private String superName;
    private String[] interfaces;
    private boolean isInterface;
    private List<ClassReference.Member> members;
    private ClassReference.Handle classHandle;
    private Set<String> annotations;
    private final Set<ClassReference> discoveredClasses;
    private final Set<MethodReference> discoveredMethods;
    private final String jar;

    public DiscoveryClassVisitor(Set<ClassReference> discoveredClasses,
                                 Set<MethodReference> discoveredMethods,
                                 String jarName) {
        super(Const.ASMVersion);
        this.discoveredClasses = discoveredClasses;
        this.discoveredMethods = discoveredMethods;
        this.jar = jarName;
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        this.name = name;
        this.superName = superName;
        this.interfaces = interfaces;
        this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
        this.members = new ArrayList<>();
        this.classHandle = new ClassReference.Handle(name);
        annotations = new HashSet<>();
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        annotations.add(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }

    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        if ((access & Opcodes.ACC_STATIC) == 0) {
            Type type = Type.getType(desc);
            String typeName;
            if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
                typeName = type.getInternalName();
            } else {
                typeName = type.getDescriptor();
            }
            members.add(new ClassReference.Member(name, access, new ClassReference.Handle(typeName)));
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {
        boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;
        Set<String> mAnno = new HashSet<>();
        MethodReference methodReference = new MethodReference(
                classHandle, name, desc, isStatic, mAnno, access, -1);
        discoveredMethods.add(methodReference);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new DiscoveryMethodAdapter(Const.ASMVersion, mv, mAnno, methodReference);
    }

    @Override
    public void visitEnd() {
        ClassReference classReference = new ClassReference(
                name,
                superName,
                Arrays.asList(interfaces),
                isInterface,
                members,
                annotations,
                jar);
        discoveredClasses.add(classReference);
        super.visitEnd();
    }
}
