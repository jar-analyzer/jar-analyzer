package com.n1ar4.agent.framework.spring.normal;

import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.SourceResultType;
import com.n1ar4.agent.dto.UrlInfo;
import com.n1ar4.agent.util.ReflectUtils;

import java.lang.reflect.Method;
import java.util.*;

public abstract class BasicMappingHandler {
    protected Object requestMappingHandler;
    protected ArrayList<UrlInfo> baseUrlInfos;
    protected ArrayList<SourceResult> sourceResults;
    protected Class MappedInterceptorClass;
    protected List<Object> adaptedInterceptors;
    protected Object requestMappingPathMatcher;

    public BasicMappingHandler() {
        sourceResults = new ArrayList<SourceResult>();
        MappedInterceptorClass = null;
        requestMappingPathMatcher = null;
    }

    public ArrayList<SourceResult> getSources(Object requestMappingHandler, ArrayList<UrlInfo> baseUrlMappings) {
        // init for now turn parse
        this.requestMappingHandler = requestMappingHandler;
        this.baseUrlInfos = baseUrlMappings;
        this.requestMappingPathMatcher = ReflectUtils.getDeclaredField(requestMappingHandler, "pathMatcher");
        ClassLoader classLoader = requestMappingHandler.getClass().getClassLoader();
        if (classLoader != null) {
            try {
                this.MappedInterceptorClass = classLoader.loadClass("org.springframework.web.servlet.handler.MappedInterceptor");
            } catch (ClassNotFoundException e) {
                System.out.println("get ClassLoader for get MappedInterceptor failed in handlerMappingResolver : " + this.getClass().getSimpleName());
            }
        }
        sourceResults.clear();

        this.getInterceptors();
        ArrayList<SourceResult> sourcesInternal = this.getSourcesInternal();
        return sourcesInternal != null ? sourcesInternal : new ArrayList<SourceResult>();
    }

    public abstract ArrayList<SourceResult> getSourcesInternal();

    public ArrayList<UrlInfo> getFixedUrlMapping(ArrayList<UrlInfo> baseUrlInfos, HashSet<String> urlPatternList, String requestInfo) {
        ArrayList<UrlInfo> nowUrlInfos = new ArrayList<UrlInfo>();
        for (UrlInfo baseUrlInfo : baseUrlInfos) {
            for (String urlPattern : urlPatternList) {
                UrlInfo newUrlInfo = new UrlInfo(baseUrlInfo);
                newUrlInfo.appendUrl(urlPattern);
                newUrlInfo.appendDescription("requestMethod : " + requestInfo);
                nowUrlInfos.add(newUrlInfo);
            }
        }
        return nowUrlInfos;
    }

    public ArrayList<UrlInfo> getFixedUrlMapping(ArrayList<UrlInfo> baseMappings, String urlPattern, String requestInfo) {
        HashSet<String> urlPatterns = new HashSet<String>(Collections.singleton(urlPattern));
        return getFixedUrlMapping(baseMappings, urlPatterns, requestInfo);
    }

    public ArrayList<UrlInfo> getFixedUrlMappingForInterceptor(ArrayList<UrlInfo> baseUrlInfos, String[] urlPatternList, String[] excludePaths) {
        ArrayList<UrlInfo> nowUrlInfos = new ArrayList<UrlInfo>();
        if (urlPatternList == null || urlPatternList.length == 0) {
            urlPatternList = new String[]{""};
        }
        for (UrlInfo baseUrlInfo : baseUrlInfos) {
            for (String urlPattern : urlPatternList) {
                UrlInfo newUrlInfo = new UrlInfo(baseUrlInfo);
                newUrlInfo.appendUrl(urlPattern);
                newUrlInfo.appendDescription("| exclude Path : " + Arrays.toString(excludePaths));
                nowUrlInfos.add(newUrlInfo);
            }
        }
        return nowUrlInfos;
    }

    protected Boolean isInPatternsForString(Object pathMatcher, String[] patterns, String lookupPath) {
        for (String pattern : patterns) {
            Boolean isMatch = (Boolean) ReflectUtils.callDeclaredMethod(pathMatcher, "match", pattern, lookupPath);
            if (isMatch == null) {
                return false;
            }
            if (isMatch == true) {
                return true;
            }
        }
        return false;
    }

    protected Boolean isInPatternsForPathAdapter(Object pathMatcher, Object[] pathAdapters, String lookupPath) {
        for (Object pathAdapter : pathAdapters) {
            String pathPattern = (String) ReflectUtils.callDeclaredMethod(pathAdapter, "getPatternString");
            Boolean isMatch = (Boolean) ReflectUtils.callDeclaredMethod(pathMatcher, "match", pathPattern, lookupPath);
            if (isMatch == null) {
                return false;
            }
            if (isMatch) return true;
        }
        return false;
    }

    protected Boolean isInPatterns(Object pathMatcher, Object[] patterns, String lookupPath) {
        if (patterns.getClass().getName().equals("[Ljava.lang.String;")) {
            return isInPatternsForString(pathMatcher, (String[]) patterns, lookupPath);
        } else {
            return isInPatternsForPathAdapter(pathMatcher, patterns, lookupPath);
        }
    }

    protected Boolean isInExcludePatterns(Object pathMatcher, Object adaptedInterceptor, String controllerPath) {
        Object[] excludePatterns = (Object[]) ReflectUtils.getDeclaredField(adaptedInterceptor, "excludePatterns");
        if (excludePatterns == null || excludePatterns.length == 0) {
            return false;
        }
        return isInPatterns(pathMatcher, excludePatterns, controllerPath);
    }


    protected Boolean isInIncludePatterns(Object pathMatcher, Object adaptedInterceptor, String controllerPath) {
        Object[] includePatterns = (Object[]) ReflectUtils.getDeclaredField(adaptedInterceptor, "includePatterns");
        if (includePatterns == null || includePatterns.length == 0) return true;

        return isInPatterns(pathMatcher, includePatterns, controllerPath);
    }

    protected ArrayList<String> getInterceptorsForPath(String lookupPath) {
        ArrayList<String> interceptorDesc = new ArrayList<String>();
        if (this.adaptedInterceptors != null && this.adaptedInterceptors.size() > 0) {
            for (Object adaptedInterceptor : this.adaptedInterceptors) {
                String interceptorName = adaptedInterceptor.getClass().getSimpleName();
                String interceptorClass = adaptedInterceptor.getClass().getName();
                if (isMappedInterceptor(adaptedInterceptor)) {
                    Object pathMatcher = ReflectUtils.getDeclaredField(adaptedInterceptor, "pathMatcher");
                    Object pathMatcherToUse = pathMatcher != null ? pathMatcher : requestMappingPathMatcher;
                    if (isInExcludePatterns(pathMatcherToUse, adaptedInterceptor, lookupPath)) continue;
                    if (isInIncludePatterns(pathMatcherToUse, adaptedInterceptor, lookupPath) == false) continue;
                    Object interceptor = ReflectUtils.callDeclaredMethod(adaptedInterceptor, "getInterceptor");
                    interceptorName = interceptor.getClass().getSimpleName();
                    interceptorClass = interceptor.getClass().getName();
                }
                if (interceptorDesc.isEmpty()) {
                    interceptorDesc.add("Interceptors for path : " + lookupPath);
                }
                interceptorDesc.add(String.format("\t%s => %s", interceptorName, interceptorClass));
            }
        }
        return interceptorDesc;
    }

    protected boolean isMappedInterceptor(Object interceptor) {
        if (this.MappedInterceptorClass != null) {
            return interceptor.getClass().isAssignableFrom(this.MappedInterceptorClass);
        }
        return "org.springframework.web.servlet.handler.MappedInterceptor".equals(interceptor.getClass().getName());
    }

    protected String[] getExcludePatternsFromInterceptor(Object interceptor) {
        Object excludePatterns = ReflectUtils.getDeclaredField(interceptor, "excludePatterns");
        if (excludePatterns == null) return null;
        if (excludePatterns.getClass().getName().equals("[Ljava.lang.String;")) {
            return (String[]) excludePatterns;
        }
        if (excludePatterns.getClass().getName().contains("PatternAdapter")) {
            ArrayList<String> excludePatternResult = new ArrayList<String>();
            for (Object excludePathAdapter : ((Object[]) excludePatterns)) {
                excludePatternResult.add((String) ReflectUtils.callDeclaredMethod(excludePathAdapter, "getPatternString"));
            }
            return excludePatternResult.toArray(new String[]{});
        }
        return null;
    }

    public void getInterceptors() {
        // interceptor info process
        List<Object> interceptors = (List<Object>) ReflectUtils.getDeclaredField(requestMappingHandler, "adaptedInterceptors");
        this.adaptedInterceptors = interceptors;
        if (interceptors != null && interceptors.isEmpty() == false) {
            for (Object interceptor : interceptors) {
                // org.springframework.web.servlet.HandlerInterceptor|preHandle|[interface javax.servlet.http.HttpServletRequest, interface javax.servlet.http.HttpServletResponse, class java.lang.Object]
                String methodInfo = "";


                if (isMappedInterceptor(interceptor)) {
                    String[] includePatterns = (String[]) ReflectUtils.callDeclaredMethod(interceptor, "getPathPatterns");
                    Object ceptor = ReflectUtils.callDeclaredMethod(interceptor, "getInterceptor");
                    String name = ceptor.getClass().getSimpleName();
                    String classname = ceptor.getClass().getName();
                    String[] excludePatterns = getExcludePatternsFromInterceptor(interceptor); // whitelist
                    Method preHandleMethod = ReflectUtils.getTargetMethodByParameterType(ceptor.getClass(), "preHandle", "[interface javax.servlet.http.HttpServletRequest, interface javax.servlet.http.HttpServletResponse, class java.lang.Object]");
                    if (preHandleMethod != null) {
                        methodInfo = ReflectUtils.getMethodInfoFromMethod(preHandleMethod);
                    }
                    sourceResults.add(new SourceResult(SourceResultType.SpringInterceptor, name, classname, methodInfo, getFixedUrlMappingForInterceptor(baseUrlInfos, includePatterns, excludePatterns)));
                } else {
                    Method preHandleMethod = ReflectUtils.getTargetMethodByParameterType(interceptor.getClass(), "preHandle", "[interface javax.servlet.http.HttpServletRequest, interface javax.servlet.http.HttpServletResponse, class java.lang.Object]");
                    if (preHandleMethod != null) {
                        methodInfo = ReflectUtils.getMethodInfoFromMethod(preHandleMethod);
                    }

                    sourceResults.add(new SourceResult(SourceResultType.SpringInterceptor, interceptor.getClass().getSimpleName(), interceptor.getClass().getName(), methodInfo, baseUrlInfos));
                }
            }
        }
    }
}
