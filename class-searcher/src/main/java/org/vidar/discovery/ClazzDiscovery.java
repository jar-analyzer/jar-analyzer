package org.vidar.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vidar.data.*;
import org.vidar.rules.ClazzRule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author zhchen
 */
public class ClazzDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClazzDiscovery.class);

    private Map<MethodReference.Handle, MethodReference> methodMap;

    private Map<ClassReference.Handle, ClassReference> classMap;

    private InheritanceMap inheritanceMap;

    private Map<MethodReference.Handle, GraphCall> callMap;
    Set<ClassReference.Handle> res = new HashSet<>();

    public void discover(List<Path> pathList, ClazzRule clazzRule) throws IOException {
        init();
        LOGGER.info("开始寻找目标类...");
        // 标志是否被filter过
        boolean flag = false;
        if (clazzRule.getMethods() != null) {
            filterByMethods(clazzRule);
            flag = true;
        }
        filterByNameType(clazzRule, flag);
        filterByParent(clazzRule, flag);
        filterByAnnotations(clazzRule, flag);
        filterByFields(clazzRule, flag);
        System.out.println("以下是搜索结果：");
        res.forEach(System.out::println);
    }

    /**
     * 根据name和type限定过滤
     *
     * @param clazzRule
     * @param flag
     */
    private void filterByNameType(ClazzRule clazzRule, boolean flag) {
        if (flag) { // 被filter过
            if (res.size() == 0) {
                return;
            }
            Set<ClassReference.Handle> toRemove = new HashSet<>();
            if (clazzRule.getName() != null) {
                Pattern pattern = Pattern.compile(clazzRule.getName());
                for (ClassReference.Handle clz : res) {
                    String[] split = clz.getName().split("/");
                    String name = split[split.length - 1];
                    if (!pattern.matcher(name).matches()) {
                        toRemove.add(clz);
                    }
                }
            }
            if (clazzRule.getIsInterface() != null) {
                for (ClassReference.Handle clz : res) {
                    if (classMap.get(clz).isInterface() != clazzRule.getIsInterface().booleanValue()) {
                        toRemove.add(clz);
                    }
                }
            }
            res.remove(toRemove);
        } else {
            flag = true;
            Set<ClassReference.Handle> toAdd = new HashSet<>();
            if (clazzRule.getName() != null || clazzRule.getIsInterface() != null) {
                for (Map.Entry<ClassReference.Handle, ClassReference> next : classMap.entrySet()) {
                    ClassReference clzRef = next.getValue();
                    if (clazzRule.getName() != null && clazzRule.getIsInterface() != null) {
                        String[] split = clzRef.getName().split("/");
                        String name = split[split.length - 1];
                        Pattern pattern = Pattern.compile(clazzRule.getName());
                        if (pattern.matcher(name).matches() && clzRef.isInterface() == clazzRule.getIsInterface().booleanValue()) {
                            toAdd.add(next.getKey());
                        }
                    } else if (clazzRule.getName() != null) {
                        String[] split = clzRef.getName().split("/");
                        String name = split[split.length - 1];
                        Pattern pattern = Pattern.compile(clazzRule.getName());
                        if (pattern.matcher(name).matches()) {
                            toAdd.add(next.getKey());
                        }
                    } else if (clazzRule.getIsInterface() != null) {
                        if (clzRef.isInterface() == clazzRule.getIsInterface().booleanValue()) {
                            toAdd.add(next.getKey());
                        }
                    }
                }
            }
            res.addAll(toAdd);
        }
    }

    /**
     * 根据field限定过滤
     *
     * @param clazzRule
     * @param flag
     */
    private void filterByFields(ClazzRule clazzRule, boolean flag) {
        if (flag) { // 被filter过
            if (res.size() == 0) {
                return;
            }
            Set<ClassReference.Handle> toRemove = new HashSet<>();
            List<ClazzRule.Field> fields = clazzRule.getFields();
            if (fields != null && fields.size() != 0) {
                for (ClassReference.Handle clz : res) {
                    ClassReference.Member[] members = classMap.get(clz).getMembers();
                    // 需要包含规则中内每个field
                    a:
                    for (ClazzRule.Field field : fields) {
                        for (ClassReference.Member member : members) {
                            if (equalsField(field, member)) {
                                continue a;
                            }
                        }
                        toRemove.add(clz);
                    }
                }
                res.removeAll(toRemove);
            }
        } else { // 没被filter过
            flag = true;
            List<ClazzRule.Field> fields = clazzRule.getFields();
            if (fields != null && fields.size() != 0) {
                for (Map.Entry<ClassReference.Handle, ClassReference> next : classMap.entrySet()) {
                    ClassReference clzRef = next.getValue();
                    ClassReference.Member[] members = clzRef.getMembers();
                    boolean toAdd = true;
                    a:
                    for (ClazzRule.Field field : fields) {
                        for (ClassReference.Member member : members) {
                            if (equalsField(field, member)) {
                                continue a;
                            }
                        }
                        toAdd = false;
                    }
                    if (toAdd) {
                        res.add(next.getKey());
                    }
                }
            }
        }
    }

    private boolean equalsField(ClazzRule.Field field, ClassReference.Member member) {
        boolean res = true;
        if (null != field.getAccess() && field.getAccess() != member.getModifiers()) {
            res = false;
        }
        if (null != field.getName() && !field.getName().equals(member.getName())) {
            res = false;
        }
        if (null != field.getType() && !field.getType().equals(member.getType().getName())) {
            res = false;
        }
        return res;
    }

    private void filterByAnnotations(ClazzRule clazzRule, boolean flag) {
        // 限定注解
        if (flag) { // 被filter过
            if (res.size() == 0) {
                return;
            }
            Set<ClassReference.Handle> toRemove = new HashSet<>();
            List<String> annotations = clazzRule.getAnnotations();
            if (annotations != null && annotations.size() != 0) {
                for (ClassReference.Handle clz : res) {
                    ClassReference classReference = classMap.get(clz);
                    Set<String> clzAnnotations = classReference.getAnnotations();
                    boolean hasAnno = false;
                    for (String annotation : annotations) {
                        if (clzAnnotations.contains(annotation)) {
                            hasAnno = true;
                            break;
                        }
                    }
                    if (!hasAnno) {
                        toRemove.add(clz);
                    }
                }
                if (toRemove.size() != 0) {
                    res.removeAll(toRemove);
                }
            }
        } else {
            flag = true;
            List<String> annotations = clazzRule.getAnnotations();
            if (annotations != null && annotations.size() != 0) {
                for (Map.Entry<ClassReference.Handle, ClassReference> next : classMap.entrySet()) {
                    ClassReference clzRef = next.getValue();
                    Set<String> clzAnnotations = clzRef.getAnnotations();
                    if (clzAnnotations != null && clzAnnotations.size() != 0) {
                        for (String annotation : annotations) {
                            // 或逻辑,只要存在其中一个注解即可
                            if (clzAnnotations.contains(annotation)) {
                                res.add(next.getKey());
                            }
                        }
                    }
                }
            }
        }

    }


    private void filterByParent(ClazzRule clazzRule, boolean flag) {
        if (flag) {
            if (res.size() == 0) {
                return;
            }
            List<String> parentLists = new ArrayList<>();
            if (clazzRule.getImplementsList() != null && clazzRule.getImplementsList().size() != 0) {
                LOGGER.info("你所寻找的class实现的接口有：");
                clazzRule.getImplementsList().forEach(p -> {
                    LOGGER.info(p);
                    parentLists.add(p);
                });
            }
            if (clazzRule.getExtendsList() != null && clazzRule.getExtendsList().size() != 0) {
                LOGGER.info("你所寻找的class继承的类有：");
                clazzRule.getExtendsList().forEach(p -> {
                    LOGGER.info(p);
                    parentLists.add(p);
                });
            }
            Set<ClassReference.Handle> toRemove = new HashSet<>();
            if (parentLists.size() != 0) {
                for (ClassReference.Handle clz : res) {
                    for (String parent : parentLists) {
                        if (!inheritanceMap.isSubclassOf(clz, new ClassReference.Handle(parent))) {
                            toRemove.add(clz);
                        }
                    }
                }
                if (toRemove.size() != 0) {
                    res.removeAll(toRemove);
                }
            }
        } else { // 没有被filter过
            flag = true;
            List<String> parentLists = new ArrayList<>();
            if (clazzRule.getImplementsList() != null && clazzRule.getImplementsList().size() != 0) {
                LOGGER.info("你所寻找的class实现的接口有：");
                clazzRule.getImplementsList().forEach(p -> {
                    LOGGER.info(p);
                    parentLists.add(p);
                });
            }
            if (clazzRule.getExtendsList() != null && clazzRule.getExtendsList().size() != 0) {
                LOGGER.info("你所寻找的class继承的类有：");
                clazzRule.getExtendsList().forEach(p -> {
                    LOGGER.info(p);
                    parentLists.add(p);
                });
            }
            // 限定parent
            if (parentLists.size() != 0) {
                for (Map.Entry<ClassReference.Handle, ClassReference> next : classMap.entrySet()) {
                    ClassReference.Handle key = next.getKey();
                    AtomicBoolean is = new AtomicBoolean(true);
                    for (String s : parentLists) {
                        if (!inheritanceMap.isSubclassOf(key, new ClassReference.Handle(s))) {
                            is.set(false);
                        }
                    }
                    if (is.get()) {
                        res.add(key);
                    }
                }
            }
        }
    }

    private void filterByMethods(ClazzRule clazzRule) {
        // 限定method
        List<ClazzRule.Method> methods = clazzRule.getMethods();
        if (methods.size() != 0) {
            methods.forEach(m -> LOGGER.info("你希望target中存在：" + m + "方法"));
            for (Map.Entry<MethodReference.Handle, MethodReference> next : methodMap.entrySet()) {
                MethodReference.Handle key = next.getKey();
                AtomicBoolean is = new AtomicBoolean(false);
                for (ClazzRule.Method m : methods) {
                    if (m.getName() != null && !key.getName().equals(m.getName())) {
                        continue;
                    }
                    if (m.getDesc() != null && !key.getDesc().equals(m.getDesc())) {
                        continue;
                    }
                    if (m.getIsStatic() != null && methodMap.get(key).isStatic() != m.getIsStatic()) {
                        continue;
                    }
                    if (m.getAccess() != null && !methodMap.get(key).getAccessModifier().equals(m.getAccess())) {
                        continue;
                    }
                    is.set(true);
                    boolean isCalls = false;
                    if (m.getCalls() != null) { // 或逻辑
                        GraphCall graphCall = callMap.get(key);
                        List<MethodReference.Handle> callMethods = graphCall.getCallMethods();
                        out:
                        for (MethodReference.Handle callMethod : callMethods) {
                            for (ClazzRule.Call call : m.getCalls()) {
                                if (callMethod.getClassReference().getName() != null ? callMethod.getClassReference().getName().equals(call.getClassRef()) : true &&
                                        callMethod.getName() != null ? callMethod.getName().equals(call.getName()) : true &&
                                        callMethod.getDesc() != null ? callMethod.getDesc().equals(call.getDesc()) : true) {
                                    isCalls = true;
                                    break out;
                                }
                            }
                        }
                        is.set(is.get() && isCalls);
                    }
                    break;
                }
                if (is.get()) {
                    res.add(key.getClassReference());
                }
            }
        }
    }

    private void init() throws IOException {
        // 加载所有方法信息
        methodMap = DataLoader.loadMethods();
        LOGGER.info("加载所有方法信息完毕...");
        // 加载所有类信息
        classMap = DataLoader.loadClasses();
        LOGGER.info("加载所有类信息完毕...");
        // 加载所有父子类、超类、实现类关系
        inheritanceMap = InheritanceMap.load();
        LOGGER.info("加载所有父子类、超类、实现类关系完毕...");
        // 加载方法调用信息
        callMap = DataLoader.loadCalls();
        LOGGER.info("加载方法调用信息完毕...");
    }

}
