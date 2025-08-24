/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.starter.Const;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.ClassReader;

import java.util.*;

public class CallGraphService {
    private static final Logger logger = LogManager.getLogger();

    public static void start(Set<CallGraph> discoveredCalls,
                             List<MethodReference.Handle> sortedMethods,
                             Map<String, ClassFileEntity> classFileByName,
                             Map<ClassReference.Handle, ClassReference> classMap,
                             Map<MethodReference.Handle, Set<CallGraph>> graphCallMap,
                             Map<MethodReference.Handle, MethodReference> methodMap,
                             Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap) {
        for (MethodReference.Handle method : sortedMethods) {
            ClassFileEntity file = classFileByName.get(method.getClassReference().getName());
            try {
                ClassReader cr = new ClassReader(file.getFile());
                CallGraphClassVisitor ccv = new CallGraphClassVisitor(discoveredCalls);
                cr.accept(ccv, Const.AnalyzeASMOptions);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        // call graph set -> call graph list
        List<CallGraph> tempList = new ArrayList<>(discoveredCalls);
        // remove unnecessary method
        for (int i = 0; i < tempList.size(); i++) {
            MethodReference.Handle target = tempList.get(i).getTargetMethod();
            if (target.getClassReference().getName().equals("java/lang/Object")) {
                if (target.getName().equals("<init>") && target.getDesc().equals("()V")) {
                    tempList.remove(tempList.get(i));
                }
            }
        }
        // resolve interface problem: interface -> impls
        for (int i = 0; i < discoveredCalls.size(); i++) {
            if (i >= tempList.size()) {
                break;
            }
            MethodReference.Handle targetMethod = tempList.get(i).getTargetMethod();
            ClassReference.Handle handle = targetMethod.getClassReference();
            ClassReference targetClass = classMap.get(handle);
            if (targetClass == null) {
                continue;
            }
            if (targetClass.isInterface()) {
                Set<MethodReference.Handle> implSet = methodImplMap.get(targetMethod);
                if (implSet == null || implSet.isEmpty()) {
                    continue;
                }
                for (MethodReference.Handle implMethod : implSet) {
                    String callerDesc = methodMap.get(implMethod).getDesc();
                    if (targetMethod.getDesc().equals(callerDesc)) {
                        tempList.add(new CallGraph(
                                targetMethod,
                                implMethod,
                                tempList.get(i).getTargetArgIndex(),
                                tempList.get(i).getTargetArgIndex()
                        ));
                    }
                }
            }
        }
        // unique
        discoveredCalls.clear();
        discoveredCalls.addAll(tempList);
        // build call graph map: method -> call graphs
        for (CallGraph graphCall : discoveredCalls) {
            MethodReference.Handle caller = graphCall.getCallerMethod();
            if (!graphCallMap.containsKey(caller)) {
                Set<CallGraph> graphCalls = new HashSet<>();
                graphCalls.add(graphCall);
                graphCallMap.put(caller, graphCalls);
            } else {
                graphCallMap.get(caller).add(graphCall);
            }
        }
    }
}
