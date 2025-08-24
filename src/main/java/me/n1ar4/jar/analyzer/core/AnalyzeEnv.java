/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.core;

import me.n1ar4.jar.analyzer.analyze.spring.SpringController;
import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.entity.ClassFileEntity;
import me.n1ar4.jar.analyzer.taint.CallGraph;

import java.util.*;

public class AnalyzeEnv {
    public static boolean isCli = false;
    public static boolean jarsInJar = false;
    public static Set<ClassFileEntity> classFileList = new HashSet<>();
    public static final Set<ClassReference> discoveredClasses = new HashSet<>();
    public static final Set<MethodReference> discoveredMethods = new HashSet<>();
    public static final Map<ClassReference.Handle, List<MethodReference>> methodsInClassMap = new HashMap<>();
    public static final Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
    public static final Map<MethodReference.Handle, MethodReference> methodMap = new HashMap<>();
    public static final HashMap<MethodReference.Handle,
            HashSet<MethodReference.Handle>> methodCalls = new HashMap<>();
    public static InheritanceMap inheritanceMap;
    public static Map<MethodReference.Handle, List<String>> strMap = new HashMap<>();
    public static ArrayList<SpringController> controllers = new ArrayList<>();
    public static ArrayList<String> interceptors = new ArrayList<>();
    public static ArrayList<String> servlets = new ArrayList<>();
    public static ArrayList<String> filters = new ArrayList<>();
    public static ArrayList<String> listeners = new ArrayList<>();
    public static Map<MethodReference.Handle, List<String>> stringAnnoMap = new HashMap<>();

    // 以下用于污点分析使用
    /**
     * CLASS NAME -> CLASS FILE
     */
    public static final Map<String, ClassFileEntity> classFileByName = new HashMap<>();
    /**
     * CALL GRAPHS
     */
    public static final Set<CallGraph> discoveredCalls = new HashSet<>();
    /**
     * METHOD NAME -> CALL GRAPHS
     */
    public static final Map<MethodReference.Handle, Set<CallGraph>> graphCallMap = new HashMap<>();
}
