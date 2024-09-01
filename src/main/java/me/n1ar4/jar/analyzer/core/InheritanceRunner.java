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
                for (ClassReference.Handle subClass : subClasses) {
                    Set<MethodReference.Handle> subClassMethods = methodsByClass.get(subClass);
                    if (subClassMethods != null) {
                        for (MethodReference.Handle subClassMethod : subClassMethods) {
                            if (subClassMethod.getName().equals(method.getName()) &&
                                    subClassMethod.getDesc().equals(method.getDesc())) {
                                overridingMethods.add(subClassMethod);
                            }
                        }
                    }
                }
            }
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
