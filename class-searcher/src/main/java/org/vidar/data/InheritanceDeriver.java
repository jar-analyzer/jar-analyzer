package org.vidar.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vidar.data.ClassReference;
import org.vidar.data.InheritanceMap;
import org.vidar.data.MethodReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InheritanceDeriver {
    private static final Logger LOGGER = LoggerFactory.getLogger(InheritanceDeriver.class);

    public static InheritanceMap derive(Map<ClassReference.Handle, ClassReference> classMap) {
        LOGGER.info("Calculating inheritance for " + (classMap.size()) + " classes...");
        Map<ClassReference.Handle, Set<ClassReference.Handle>> implicitInheritance = new HashMap<>();
        //遍历所有类
        for (ClassReference classReference : classMap.values()) {
            if (implicitInheritance.containsKey(classReference.getHandle())) {
                throw new IllegalStateException("Already derived implicit classes for " + classReference.getName());
            }
            Set<ClassReference.Handle> allParents = new HashSet<>();

            //获取classReference的所有父类、超类、接口类
            getAllParents(classReference, classMap, allParents);
            //添加缓存：类名 -> 所有的父类、超类、接口类
            implicitInheritance.put(classReference.getHandle(), allParents);
        }
        //InheritanceMap翻转集合，转换为{class:[subclass]}
        return new InheritanceMap(implicitInheritance);
    }

    /**
     * 获取classReference的所有父类、超类、接口类
     *
     * @param classReference
     * @param classMap
     * @param allParents
     */
    private static void getAllParents(ClassReference classReference, Map<ClassReference.Handle, ClassReference> classMap, Set<ClassReference.Handle> allParents) {
        Set<ClassReference.Handle> parents = new HashSet<>();
        //把当前classReference类的父类添加到parents
        if (classReference.getSuperClass() != null) {
            parents.add(new ClassReference.Handle(classReference.getSuperClass()));
        }
        //把当前classReference类实现的所有接口添加到parents
        for (String iface : classReference.getInterfaces()) {
            parents.add(new ClassReference.Handle(iface));
        }

        for (ClassReference.Handle immediateParent : parents) {
            //从所有类数据集合中，遍历找出classReference的父类、接口
            ClassReference parentClassReference = classMap.get(immediateParent);
            if (parentClassReference == null) {
                LOGGER.debug("No class id for " + immediateParent.getName());
                continue;
            }
            //继续添加到集合中
            allParents.add(parentClassReference.getHandle());
            //继续递归查找，直到把classReference类的所有父类、超类、接口类都添加到allParents
            getAllParents(parentClassReference, classMap, allParents);
        }
    }

    public static Map<MethodReference.Handle, Set<MethodReference.Handle>> getAllMethodImplementations(
            InheritanceMap inheritanceMap, Map<MethodReference.Handle, MethodReference> methodMap) {

        //遍历整合，得到每个类的所有方法实现，形成 类->实现的方法集 的映射
        Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = getMethodsByClass(methodMap);

        //遍历继承关系数据，形成 父类->子孙类集 的映射
        Map<ClassReference.Handle, Set<ClassReference.Handle>> subClassMap = new HashMap<>();
        for (Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>> entry : inheritanceMap.entrySet()) {
            for (ClassReference.Handle parent : entry.getValue()) {
                if (!subClassMap.containsKey(parent)) {
                    Set<ClassReference.Handle> subClasses = new HashSet<>();
                    subClasses.add(entry.getKey());
                    subClassMap.put(parent, subClasses);
                } else {
                    subClassMap.get(parent).add(entry.getKey());
                }
            }
        }

        //遍历所有方法，根据父类->子孙类集合，找到所有的override的方法，记录下来（某个类的方法->所有的override方法）
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap = new HashMap<>();
        for (MethodReference method : methodMap.values()) {
            // Static methods cannot be overriden
            if (method.isStatic()) {
                continue;
            }

            Set<MethodReference.Handle> overridingMethods = new HashSet<>();
            Set<ClassReference.Handle> subClasses = subClassMap.get(method.getClassReference());
            if (subClasses != null) {
                for (ClassReference.Handle subClass : subClasses) {
                    // This class extends ours; see if it has a matching method
                    Set<MethodReference.Handle> subClassMethods = methodsByClass.get(subClass);
                    if (subClassMethods != null) {
                        for (MethodReference.Handle subClassMethod : subClassMethods) {
                            if (subClassMethod.getName().equals(method.getName()) && subClassMethod.getDesc().equals(method.getDesc())) {
                                overridingMethods.add(subClassMethod);
                            }
                        }
                    }
                }
            }

            if (overridingMethods.size() > 0) {
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
