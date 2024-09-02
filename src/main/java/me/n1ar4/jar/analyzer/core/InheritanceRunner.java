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

package me.n1ar4.jar.analyzer.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class InheritanceRunner {
    public static InheritanceMap derive(Map<ClassReference.Handle, ClassReference> classMap) {
        Map<ClassReference.Handle, Set<ClassReference.Handle>> implicitInheritance = new HashMap<>();
        for (ClassReference classReference : classMap.values()) {
            Set<ClassReference.Handle> allParents = new HashSet<>();
            getAllParents(classReference, classMap, allParents);
            implicitInheritance.put(classReference.getHandle(), allParents);
        }
        return new InheritanceMap(implicitInheritance);
    }

    private static void getAllParents(ClassReference classReference,
                                      Map<ClassReference.Handle, ClassReference> classMap,
                                      Set<ClassReference.Handle> allParents) {
        Set<ClassReference.Handle> parents = new HashSet<>();
        if (classReference.getSuperClass() != null) {
            parents.add(new ClassReference.Handle(classReference.getSuperClass()));
        }
        for (String i : classReference.getInterfaces()) {
            parents.add(new ClassReference.Handle(i));
        }

        for (ClassReference.Handle immediateParent : parents) {
            ClassReference parentClassReference = classMap.get(immediateParent);
            if (parentClassReference == null) {
                continue;
            }
            allParents.add(parentClassReference.getHandle());
            getAllParents(parentClassReference, classMap, allParents);
        }
    }

    public static Map<MethodReference.Handle, Set<MethodReference.Handle>> getAllMethodImplementations(
            InheritanceMap inheritanceMap, Map<MethodReference.Handle, MethodReference> methodMap) {
        Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = getMethodsByClass(methodMap);
        Map<ClassReference.Handle, Set<ClassReference.Handle>> subClassMap = inheritanceMap.getSubClassMap();
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap = new HashMap<>();
        for (MethodReference method : methodMap.values()) {
            if (method.isStatic()) {
                continue;
            }
            Set<MethodReference.Handle> overridingMethods = new HashSet<>();
            Set<ClassReference.Handle> subClasses = subClassMap.get(method.getClassReference());
            if (subClasses != null) {
                // 遍历某个方法的所有子类
                for (ClassReference.Handle subClass : subClasses) {
                    Set<MethodReference.Handle> subClassMethods = methodsByClass.get(subClass);
                    if (subClassMethods != null) {
                        // 所有子类的方法
                        for (MethodReference.Handle subClassMethod : subClassMethods) {
                            // 子类方法名和 DESC 完全一致
                            // 这是 override 方法
                            if (subClassMethod.getName().equals(method.getName()) &&
                                    subClassMethod.getDesc().equals(method.getDesc())) {
                                overridingMethods.add(subClassMethod);
                            }
                        }
                    }
                }
            }
            // 方法 -> [所有子类 override 方法列表]
            if (!overridingMethods.isEmpty()) {
                methodImplMap.put(method.getHandle(), overridingMethods);
            }
        }
        return methodImplMap;
    }

    public static Map<ClassReference.Handle, Set<MethodReference.Handle>> getMethodsByClass(
            Map<MethodReference.Handle, MethodReference> methodMap) {
        Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = new HashMap<>();
        for (MethodReference.Handle method : methodMap.keySet()) {
            ClassReference.Handle classReference = method.getClassReference();
            if (!methodsByClass.containsKey(classReference)) {
                Set<MethodReference.Handle> methods = new HashSet<>();
                methods.add(method);
                methodsByClass.put(classReference, methods);
            } else {
                methodsByClass.get(classReference).add(method);
            }
        }
        return methodsByClass;
    }
}
