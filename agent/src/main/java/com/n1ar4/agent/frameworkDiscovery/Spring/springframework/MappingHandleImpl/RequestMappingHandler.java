package com.n1ar4.agent.frameworkDiscovery.Spring.springframework.MappingHandleImpl;

import com.n1ar4.agent.Utils.ReflectUtils;
import com.n1ar4.agent.frameworkDiscovery.Spring.springframework.BasicMappingHandler;
import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.SourceResultType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class RequestMappingHandler extends BasicMappingHandler {

    public RequestMappingHandler() {
        super();
    }

    public ArrayList<SourceResult> getSourcesInternal() {
        getAllController();
//        sourceResults.getClass().getResourceAsStream()
        return sourceResults;
    }

    public String getSpringVersion() {
        String springMvcJarPath = requestMappingHandler.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (springMvcJarPath == null || springMvcJarPath.equals(""))
            return "";
        File file = new File(springMvcJarPath);
        if (file == null)
            return "";

        try {
            JarFile jarFile = new JarFile(file);
            if (jarFile == null)
                return "";
            Manifest manifest = jarFile.getManifest();
            if (manifest == null)
                return "";

            String attributeValue = manifest.getMainAttributes().getValue("Implementation-Version");
            if (attributeValue == null)
                return "";
            return attributeValue;
        } catch (IOException e) {

            return "";
        }
    }

    private void getAllController() {
        Object mappingRegistry = ReflectUtils.getDeclaredField(requestMappingHandler, "mappingRegistry");
        if (mappingRegistry == null)
            return;

        String springVersion = getSpringVersion();
        // for Spring v5
        if (springVersion.equals("") || springVersion.startsWith("5.")) {
            if (getControllerForSpringV5(mappingRegistry)) {
                return;
            }
        }

        if (springVersion.equals("") || springVersion.startsWith("4.")) {
            if (getControllerForSpringV4(mappingRegistry)) {
                return;
            }
        }
    }

    private boolean getControllerForSpringV4(Object mappingRegistry) {
        // for Spring v4
        HashMap<Object, Object> mappingLookup = (HashMap<Object, Object>) ReflectUtils.getDeclaredField(mappingRegistry, "mappingLookup"); // requestInfo , HandlerMethod
        // mappingLookup
        if (mappingLookup == null)
            return false;
        for (Map.Entry<Object, Object> requestMappingEntry : mappingLookup.entrySet()) {
            Object requestInfo = requestMappingEntry.getKey();

            Object handlerMethod = requestMappingEntry.getValue();
            Method targetMethod = (Method) ReflectUtils.callDeclaredMethod(handlerMethod, "getMethod");
            String sourceClass = targetMethod.getDeclaringClass().getName();
            String methodInfo = ReflectUtils.getMethodInfoFromMethod(targetMethod);


            Object methodsCondition = ReflectUtils.callDeclaredMethod(requestInfo, "getMethodsCondition");
            Object requestMethods = ReflectUtils.callDeclaredMethod(methodsCondition, "getMethods");
            String requestMethodString = requestMethods.toString();
            Object oPatternsCondition = ReflectUtils.callDeclaredMethod(requestInfo, "getPatternsCondition");
            Set<String> urlPatternList = (Set<String>) ReflectUtils.callDeclaredMethod(oPatternsCondition, "getPatterns");
            ArrayList<String> descForController = new ArrayList<String>();
            for (String urlPattern : urlPatternList) {
                descForController.addAll(getInterceptorsForPath(urlPattern));
            }
            sourceResults.add(new SourceResult(SourceResultType.SpringRequestMappingController, targetMethod.getDeclaringClass().getSimpleName(), sourceClass, methodInfo, getFixedUrlMapping(baseUrlInfos, new HashSet<String>(urlPatternList), requestMethodString), descForController));
        }

        return true;
    }

    public boolean getControllerForSpringV5(Object mappingRegistry) {
        HashMap<Object, Object> registrations = (HashMap<Object, Object>) ReflectUtils.getDeclaredField(mappingRegistry, "registry"); // requestInfo , HandlerMethod
        if (registrations == null) return false;
        for (Map.Entry<Object, Object> requestMappingEntry : registrations.entrySet()) {
            Object requestInfo = requestMappingEntry.getKey();

            Object mappginRegistration = requestMappingEntry.getValue();
            Object handlerMethod = ReflectUtils.callDeclaredMethod(mappginRegistration, "getHandlerMethod");
//            if(handlerMethod == null)
//                handlerMethod = mappginRegistration; // for v5.2 version
            Method targetMethod = (Method) ReflectUtils.callDeclaredMethod(handlerMethod, "getMethod");

            String sourceClass = targetMethod.getDeclaringClass().getName();
            String methodInfo = ReflectUtils.getMethodInfoFromMethod(targetMethod);

            Object methodsCondition = ReflectUtils.callDeclaredMethod(requestInfo, "getMethodsCondition");
            Object requestMethods = ReflectUtils.callDeclaredMethod(methodsCondition, "getMethods");
            String requestMethodString = requestMethods.toString();
            HashSet<Object> urlPatternList = getUrlPatternListFromRequestInfo(requestInfo);
            ArrayList<String> descForController = new ArrayList<String>();
            HashSet<String> urlSet = new HashSet<String>();
            for (Object urlPattern : urlPatternList) {
                String nowUrlPattern = null;
                if (urlPattern.getClass().getName().contains("PathPattern")) {
//                    urlPattern = ((PathPattern) urlPattern).getPatternString();
                    descForController.addAll(getInterceptorsForPath((String) ReflectUtils.callDeclaredMethod(urlPattern, "getPatternString")));
                    nowUrlPattern = (String) ReflectUtils.callDeclaredMethod(urlPattern, "getPatternString");
                } else {
                    descForController.addAll(getInterceptorsForPath((String) urlPattern));
                    nowUrlPattern = (String) urlPattern;
                }
                urlSet.add(nowUrlPattern);
            }

//            urlSet.addAll(urlPatte);
            sourceResults.add(new SourceResult(SourceResultType.SpringRequestMappingController, targetMethod.getDeclaringClass().getSimpleName(), sourceClass, methodInfo, getFixedUrlMapping(baseUrlInfos, urlSet, requestMethodString), descForController));
        }
        return true;
    }


    public HashSet<Object> getUrlPatternListFromRequestInfo(Object requestInfo) {
        Object targetPatterCondition = null;

        targetPatterCondition = ReflectUtils.getDeclaredField(requestInfo, "pathPatternsCondition");
        if (targetPatterCondition != null) {
            try {
                return (HashSet<Object>) ReflectUtils.callDeclaredMethod(targetPatterCondition, "getDirectPaths");
            } catch (ClassCastException e) {
                return new HashSet((Set) ReflectUtils.callDeclaredMethod(targetPatterCondition, "getPatterns", new Object[0]));
            }
        }

        targetPatterCondition = ReflectUtils.getDeclaredField(requestInfo, "patternsCondition");
        if (targetPatterCondition != null)
            return new HashSet<Object>((Set<String>) ReflectUtils.callDeclaredMethod(targetPatterCondition, "getPatterns"));
        return new HashSet<Object>(Collections.singleton(requestInfo.toString()));
    }

}
