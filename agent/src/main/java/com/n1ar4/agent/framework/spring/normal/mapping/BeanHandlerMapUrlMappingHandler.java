package com.n1ar4.agent.framework.spring.normal.mapping;

import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.SourceResultType;
import com.n1ar4.agent.framework.spring.normal.BasicMappingHandler;
import com.n1ar4.agent.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BeanHandlerMapUrlMappingHandler extends BasicMappingHandler {
    public BeanHandlerMapUrlMappingHandler() {
        super();
    }

    @Override
    public ArrayList<SourceResult> getSourcesInternal() {
        Map<String, Object> handlerMap = (Map<String, Object>) ReflectUtils.getDeclaredField(requestMappingHandler, "handlerMap");
        if (handlerMap == null) {
            return null;
        }

        HashMap<Object, ArrayList<String>> objectUrlPatternMap = new HashMap<Object, ArrayList<String>>();

        for (Map.Entry<String, Object> handlerMapEntry : handlerMap.entrySet()) {
            String urlPattern = handlerMapEntry.getKey();
            Object controllerObject = handlerMapEntry.getValue();
            if (controllerObject == null)
                continue;
            if (objectUrlPatternMap.containsKey(controllerObject) == false)
                objectUrlPatternMap.put(controllerObject, new ArrayList<String>());
            objectUrlPatternMap.get(controllerObject).add(urlPattern);
        }

        for (Map.Entry<Object, ArrayList<String>> objectArrayListEntry : objectUrlPatternMap.entrySet()) {
            Object controllerObject = objectArrayListEntry.getKey();
            ArrayList<String> urlPatterns = objectArrayListEntry.getValue();
            String sourceClass = controllerObject.getClass().getName();

            ArrayList<String> descForController = new ArrayList<String>();
            for (String urlPattern : urlPatterns) {
                descForController.addAll(getInterceptorsForPath(urlPattern));
            }
            Class controllerClass = controllerObject.getClass();
            Method handleRequestMethod = ReflectUtils.getTargetMethodByParameterType(controllerClass, "handleRequest", "[interface javax.servlet.http.HttpServletRequest, interface javax.servlet.http.HttpServletResponse]");
            String handleRequestMethodInfo = "";
            if (handleRequestMethod != null) {
                handleRequestMethodInfo = ReflectUtils.getMethodInfoFromMethod(handleRequestMethod);
                if (!handleRequestMethod.getDeclaringClass().getName().equals(controllerClass.getName())) {
                    descForController.add("target Method in class : " + handleRequestMethod.getDeclaringClass().getName());
                }
            }
            SourceResult sourceResult = new SourceResult(SourceResultType.SpringBeanNameController, controllerObject.getClass().getSimpleName(), sourceClass, handleRequestMethodInfo, getFixedUrlMapping(baseUrlInfos, new HashSet<String>(urlPatterns), "[]"), descForController);
            sourceResults.add(sourceResult);
        }

        return sourceResults;
    }


}
