package com.n1ar4.agent.framework.spring.normal.mapping;

import com.n1ar4.agent.util.ReflectUtils;
import com.n1ar4.agent.framework.spring.normal.BasicMappingHandler;
import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.SourceResultType;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class RouterFunctionMappingHandler extends BasicMappingHandler {
    public RouterFunctionMappingHandler() {
        super();
    }

    private void processDefaultRouterFunction(Object node, String pubPattern) {
        String pattern = "";
        String reqMethod = "";

        Object predicate = ReflectUtils.getDeclaredField(node, "predicate");
        if (predicate != null) {
            Object left = ReflectUtils.getDeclaredField(predicate, "left");
            Object right = ReflectUtils.getDeclaredField(predicate, "right");
            reqMethod = (String) ReflectUtils.callDeclaredMethod(left, "toString");
            pattern = (String) ReflectUtils.callDeclaredMethod(right, "toString");
        }

        pattern = String.format("%s%s", pubPattern, pattern);
        Object handlerFunction = ReflectUtils.getDeclaredField(node, "handlerFunction");
        if (handlerFunction == null) {
            return;
        }
        String routerFuncClass = handlerFunction.getClass().getName().split("\\$")[0];
        String routerFuncClassN = handlerFunction.getClass().getSimpleName().split("\\$")[0];
        ArrayList<String> interceptorsForPath = getInterceptorsForPath(pattern);
        // ReflectUtils.getTargetMethodByParameterType(handlerFunction.getClass() , "handle" , "[interface org.springframework.web.servlet.function.ServerRequest]")
        Method targetMethod = ReflectUtils.getTargetMethodByParameterType(handlerFunction.getClass(), "handle", "[interface org.springframework.web.servlet.function.ServerRequest]");
        String methodInfo = "";
        if (targetMethod != null) {
            methodInfo = ReflectUtils.getMethodInfoFromMethod(targetMethod);
        }
        sourceResults.add(new SourceResult(SourceResultType.SpringRouterFunctionMapping, routerFuncClassN, routerFuncClass, methodInfo, getFixedUrlMapping(baseUrlInfos, pattern, reqMethod), interceptorsForPath));
    }

    private String getPubPattern(Object node) {
        String pubPattern = "";
        Object predicate = ReflectUtils.getDeclaredField(node, "predicate");
        if (predicate != null) {
            Object pattern = ReflectUtils.getDeclaredField(predicate, "pattern");
            if (pattern != null) {
                pubPattern = (String) ReflectUtils.callDeclaredMethod(pattern, "getPatternString");
            }
        }
        return pubPattern;
    }

    private void nodeTraverse(Object node) {
        Object first = null;
        Object second = null;
        for (first = ReflectUtils.getDeclaredField(node, "first"), second = ReflectUtils.getDeclaredField(node, "second"); first != null; node = first, first = ReflectUtils.getDeclaredField(node, "first"), second = ReflectUtils.getDeclaredField(node, "second")) {
            processRouterFunction(second);
        }

        processRouterFunction(node);
    }

    public void processRouterFunction(Object node) {
        String pubPattern = "";
        Object rootNode = node;
        Object routerFunction = ReflectUtils.getDeclaredField(node, "routerFunction");
        if (routerFunction != null) {
            pubPattern = getPubPattern(node);
            rootNode = routerFunction;
        }

        Object first = null;
        Object second = null;
        for (first = ReflectUtils.getDeclaredField(rootNode, "first"), second = ReflectUtils.getDeclaredField(rootNode, "second"); first != null; rootNode = first, first = ReflectUtils.getDeclaredField(rootNode, "first"), second = ReflectUtils.getDeclaredField(rootNode, "second")) {
            processDefaultRouterFunction(second, pubPattern);
        }

        processDefaultRouterFunction(rootNode, pubPattern);
    }

    @Override
    public ArrayList<SourceResult> getSourcesInternal() {
        Object routerFunction = ReflectUtils.getDeclaredField(requestMappingHandler, "routerFunction");
        if (routerFunction == null) {
            return null;
        }
        nodeTraverse(routerFunction);
        return sourceResults;
    }
}
