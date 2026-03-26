/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.el;

import me.n1ar4.jar.analyzer.core.reference.AnnoReference;
import me.n1ar4.jar.analyzer.core.reference.ClassReference;
import me.n1ar4.jar.analyzer.core.reference.MethodReference;
import me.n1ar4.jar.analyzer.entity.MethodResult;
import me.n1ar4.jar.analyzer.gui.MainForm;
import me.n1ar4.log.LogManager;
import me.n1ar4.log.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MethodELProcessor {
    private static final Logger logger = LogManager.getLogger();

    private final ClassReference.Handle ch;
    private final MethodReference mr;
    private final ConcurrentLinkedQueue<ResObj> searchList;
    private final MethodEL condition;

    private final Set<ClassReference.Handle> subs;
    private final Set<ClassReference.Handle> supers;

    public MethodELProcessor(ClassReference.Handle ch, MethodReference mr,
                             ConcurrentLinkedQueue<ResObj> searchList, MethodEL condition) {
        this.ch = ch.cloneObj();
        this.mr = mr.cloneObj();
        this.searchList = searchList;
        // condition 无需 clone 不存在冲突
        this.condition = condition;
        this.subs = new HashSet<>(MainForm.getEngine().getSuperClasses(ch));
        this.supers = new HashSet<>(MainForm.getEngine().getSubClasses(ch));
    }

    public void process() {
        String classCon = condition.getClassNameContains();
        String classNoCon = condition.getClassNameNotContains();
        String mnCon = condition.getNameContains();
        String mnNoCon = condition.getNameNotContains();
        String retCon = condition.getReturnType();
        Map<Integer, String> paramMap = condition.getParamTypes();

        String methodAnno = condition.getMethodAnno();
        String excludedMethodAnno = condition.getExcludedMethodAnno();
        String classAnno = condition.getClassAnno();

        String isSubOf = condition.getIsSubClassOf();
        String isSuperOf = condition.getIsSuperClassOf();

        String start = condition.getStartWith();
        String endWith = condition.getEndWith();

        String hasField = condition.getField();

        List<String[]> containsInvokeList = condition.getContainsInvokeList();
        List<String[]> excludeInvokeList = condition.getExcludeInvokeList();
        String nameRegexStr = condition.getNameRegex();
        String classNameRegexStr = condition.getClassNameRegex();

        Integer i = condition.getParamsNum();
        Boolean f = condition.getStatic();
        Boolean p = condition.getPublic();
        int paramNum = Type.getMethodType(mr.getDesc()).getArgumentTypes().length;
        String ret = Type.getReturnType(mr.getDesc()).getClassName();

        boolean classNameContainsFlag = true;
        boolean classNameNotContainsFlag = true;
        boolean methodNameContainsFlag = true;
        boolean methodNameNotContainsFlag = true;
        boolean paramNumFlag = true;
        boolean retTypeFlag = true;
        boolean isStaticFlag = true;
        boolean isPublicFlag = true;
        boolean paramMapFlag = true;
        boolean classAnnoFlag = true;
        boolean methodAnnoFlag = true;
        boolean fieldFlag = true;
        boolean subClassFlag = true;
        boolean superClassFlag = true;
        boolean startWithFlag = true;
        boolean endWithFlag = true;
        boolean containsInvokeFlag = true;
        boolean excludeInvokeFlag = true;
        boolean nameRegexFlag = true;
        boolean classNameRegexFlag = true;

        if (classCon != null && !classCon.isEmpty()) {
            classNameContainsFlag = ch.getName().contains(classCon);
        }
        if (classNoCon != null && !classNoCon.isEmpty()) {
            classNameNotContainsFlag = !ch.getName().contains(classNoCon);
        }

        if (mnCon != null && !mnCon.isEmpty()) {
            methodNameContainsFlag = mr.getName().contains(mnCon);
        }
        if (mnNoCon != null && !mnNoCon.isEmpty()) {
            methodNameNotContainsFlag = !mr.getName().contains(mnNoCon);
        }

        ClassReference cr = MainForm.getEngine().getClassRef(ch, mr.getJarId());

        if (classAnno != null && !classAnno.isEmpty()) {
            if (cr.getAnnotations() == null ||
                    cr.getAnnotations().isEmpty()) {
                classAnnoFlag = false;
            } else {
                boolean fc = false;
                for (AnnoReference a : cr.getAnnotations()) {
                    if (a.getAnnoName().contains(classAnno)) {
                        fc = true;
                        break;
                    }
                }
                if (!fc) {
                    classAnnoFlag = false;
                }
            }
        }

        if (methodAnno != null && !methodAnno.isEmpty()) {
            if (mr.getAnnotations() == null || mr.getAnnotations().isEmpty()) {
                methodAnnoFlag = false;
            } else {
                boolean fm = false;
                for (AnnoReference a : mr.getAnnotations()) {
                    if (a.getAnnoName().contains(methodAnno)) {
                        fm = true;
                        break;
                    }
                }
                if (!fm) {
                    methodAnnoFlag = false;
                }
            }
        }

        boolean isExcludedMethod = isExcludedMethodAnno(excludedMethodAnno);

        if (start != null && !start.isEmpty()) {
            startWithFlag = mr.getName().startsWith(start);
        }

        if (endWith != null && !endWith.isEmpty()) {
            endWithFlag = mr.getName().endsWith(endWith);
        }

        if (hasField != null && !hasField.isEmpty()) {
            boolean ff = false;
            for (ClassReference.Member m : cr.getMembers()) {
                if (m.getName().contains(hasField)) {
                    ff = true;
                    break;
                }
            }
            if (!ff) {
                fieldFlag = false;
            }
        }

        // 方法名正则匹配
        if (nameRegexStr != null && !nameRegexStr.isEmpty()) {
            try {
                nameRegexFlag = Pattern.matches(nameRegexStr, mr.getName());
            } catch (PatternSyntaxException ex) {
                logger.error("invalid name regex: {}", nameRegexStr);
                nameRegexFlag = false;
            }
        }

        // 类名正则匹配
        if (classNameRegexStr != null && !classNameRegexStr.isEmpty()) {
            try {
                classNameRegexFlag = Pattern.matches(classNameRegexStr, ch.getName());
            } catch (PatternSyntaxException ex) {
                logger.error("invalid class name regex: {}", classNameRegexStr);
                classNameRegexFlag = false;
            }
        }

        // 方法体内调用检查（containsInvoke / excludeInvoke）
        // 共享同一次 callee 查询，避免重复数据库访问
        boolean needCallees = (containsInvokeList != null && !containsInvokeList.isEmpty())
                || (excludeInvokeList != null && !excludeInvokeList.isEmpty());
        ArrayList<MethodResult> callees = null;
        if (needCallees) {
            callees = MainForm.getEngine().getCallee(
                    ch.getName(), mr.getName(), mr.getDesc());
        }

        if (containsInvokeList != null && !containsInvokeList.isEmpty()) {
            for (String[] invokeTarget : containsInvokeList) {
                String targetClass = invokeTarget[0].replace(".", "/");
                String targetMethod = invokeTarget[1];
                boolean found = false;
                for (MethodResult callee : callees) {
                    if (callee.getClassName().equals(targetClass) &&
                            callee.getMethodName().equals(targetMethod)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    containsInvokeFlag = false;
                    break;
                }
            }
        }

        // 方法体内调用排除（excludeInvoke）
        if (excludeInvokeList != null && !excludeInvokeList.isEmpty()) {
            for (String[] invokeTarget : excludeInvokeList) {
                String targetClass = invokeTarget[0].replace(".", "/");
                String targetMethod = invokeTarget[1];
                for (MethodResult callee : callees) {
                    if (callee.getClassName().equals(targetClass) &&
                            callee.getMethodName().equals(targetMethod)) {
                        excludeInvokeFlag = false;
                        break;
                    }
                }
                if (!excludeInvokeFlag) {
                    break;
                }
            }
        }

        if (i != null) {
            paramNumFlag = i == paramNum;
        }

        if (retCon != null && !retCon.isEmpty()) {
            retTypeFlag = ret.equals(retCon);
        }

        if (f != null) {
            isStaticFlag = f == mr.isStatic();
        }

        if (p != null) {
            boolean isPublic = (mr.getAccess() & Opcodes.ACC_PUBLIC) != 0;
            isPublicFlag = p == isPublic;
        }

        if (isSubOf != null && !isSubOf.isEmpty()) {
            isSubOf = isSubOf.replace(".", "/");
            if (subs != null && !subs.isEmpty()) {
                boolean t = false;
                for (ClassReference.Handle h : subs) {
                    if (h.getName().equals(isSubOf)) {
                        t = true;
                        break;
                    }
                }
                if (!t) {
                    subClassFlag = false;
                }
            } else {
                subClassFlag = false;
            }
        }
        if (isSuperOf != null && !isSuperOf.isEmpty()) {
            isSuperOf = isSuperOf.replace(".", "/");
            if (supers != null && !supers.isEmpty()) {
                boolean t = false;
                for (ClassReference.Handle h : supers) {
                    if (h.getName().equals(isSuperOf)) {
                        t = true;
                        break;
                    }
                }
                if (!t) {
                    superClassFlag = false;
                }
            } else {
                subClassFlag = false;
            }
        }
        Type[] argTypes = Type.getArgumentTypes(mr.getDesc());
        for (int ix = 0; ix < argTypes.length; ix++) {
            String temp = paramMap.get(ix);
            if (temp == null) {
                continue;
            }
            if (!paramMap.get(ix).equals(argTypes[ix].getClassName())) {
                paramMapFlag = false;
                break;
            }
        }
        if (classNameContainsFlag && methodNameContainsFlag &&
                classNameNotContainsFlag && methodNameNotContainsFlag &&
                isStaticFlag && isPublicFlag &&
                paramNumFlag && retTypeFlag && paramMapFlag &&
                classAnnoFlag && methodAnnoFlag &&
                fieldFlag &&
                subClassFlag && superClassFlag &&
                startWithFlag && endWithFlag &&
                containsInvokeFlag && excludeInvokeFlag &&
                nameRegexFlag && classNameRegexFlag &&
                !isExcludedMethod) {
            logger.info("found result {} - {}", ch.getName(), mr.getName());
            searchList.add(new ResObj(mr.getHandle(), ch.getName(), mr.getLineNumber()));
        }
    }

    private boolean isExcludedMethodAnno(String excludedMethodAnno) {
        if (mr.getAnnotations() == null || mr.getAnnotations().isEmpty()) {
            return false;
        }
        if (excludedMethodAnno == null || excludedMethodAnno.isEmpty()) {
            return false;
        }
        boolean isExcludedMethodAnno = false;
        for (AnnoReference annotation : mr.getAnnotations()) {
            if (annotation.getAnnoName().contains(excludedMethodAnno)) {
                isExcludedMethodAnno = true;
                break;
            }
        }
        return isExcludedMethodAnno;
    }
}
