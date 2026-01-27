package com.n1ar4.agent.framework.spring.normal;

import com.n1ar4.agent.util.ReflectUtils;
import com.n1ar4.agent.framework.FrameworkResolver;
import com.n1ar4.agent.framework.spring.normal.mapping.BeanHandlerMapUrlMappingHandler;
import com.n1ar4.agent.framework.spring.normal.mapping.RequestMappingHandler;
import com.n1ar4.agent.framework.spring.normal.mapping.RouterFunctionMappingHandler;
import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.UrlInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpringInfo extends FrameworkResolver {
    protected HashMap<String, BasicMappingHandler> handlerMappingHandlerMap;

    public SpringInfo(String resolverClass) {
        super(resolverClass);
        this.handlerMappingHandlerMap = new HashMap<String, BasicMappingHandler>();
        this.handlerMappingHandlerMap.
                put("org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping", new RequestMappingHandler());
        this.handlerMappingHandlerMap.
                put("org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping", new BeanHandlerMapUrlMappingHandler());
        this.handlerMappingHandlerMap.
                put("org.springframework.web.servlet.handler.SimpleUrlHandlerMapping", new BeanHandlerMapUrlMappingHandler());
        this.handlerMappingHandlerMap.
                put("org.springframework.web.servlet.function.support.RouterFunctionMapping", new RouterFunctionMappingHandler());
    }


    @Override
    protected ArrayList<SourceResult> resolveInternal(Object instance, ArrayList<UrlInfo> baseUrlMappings) {
        ArrayList<SourceResult> sourceResults = new ArrayList<SourceResult>();
        List<Object> handlerMappings = (List<Object>) ReflectUtils.getDeclaredField(instance, "handlerMappings");


        if (handlerMappings != null) {
            for (Object mapping : handlerMappings) {
                String mappingName = mapping.getClass().getName();
                if (this.handlerMappingHandlerMap.containsKey(mappingName) == false) {
                    continue;
                }
                sourceResults.addAll(handlerMappingHandlerMap.get(mappingName).getSources(mapping, baseUrlMappings));
            }
        }

        return sourceResults;
    }
}
