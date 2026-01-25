package com.n1ar4.agent.service.IOUnderTow.HandlerResolvers;

import com.n1ar4.agent.service.IOUnderTow.HandlerResolvers.Impls.DefaultHandler;
import com.n1ar4.agent.service.IOUnderTow.HandlerResolvers.Impls.DeploymentHandler;
import com.n1ar4.agent.service.IOUnderTow.HandlerResolvers.Impls.HttpContinueReadHandler;
import com.n1ar4.agent.service.IOUnderTow.HandlerResolvers.Impls.ServletInitialHandler;

import java.util.HashMap;

public class HandlerMap {
    private static HashMap<String , BasicHandlerResolver> HandlerMap;
    static {
        HandlerMap = new HashMap<String, BasicHandlerResolver>();
        HandlerMap.put("io.undertow.servlet.handlers.ServletInitialHandler" , new ServletInitialHandler());
        HandlerMap.put("org.springframework.boot.web.embedded.undertow.DeploymentManagerHttpHandlerFactory$DeploymentManagerHandler" , new DeploymentHandler());
        HandlerMap.put("io.undertow.server.handlers.HttpContinueReadHandler" , new HttpContinueReadHandler());
        HandlerMap.put("default" , new DefaultHandler());

    }

    public static BasicHandlerResolver getHandlerResolverByInstance(Object handler){
        String handlerKey = "default";
        if(handler != null && HandlerMap.containsKey(handler.getClass().getName())){
            handlerKey = handler.getClass().getName();
        }
        return HandlerMap.get(handlerKey);
    }
}
