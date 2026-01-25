package com.n1ar4.agent.service.IOUnderTow.HandlerResolvers.Impls;

import com.n1ar4.agent.service.IOUnderTow.HandlerResolvers.BasicHandlerResolver;
import com.n1ar4.agent.service.IOUnderTow.urlInfo.UnderTowConnectorInfo;

public class DefaultHandler extends BasicHandlerResolver {
    @Override
    protected void resolverInternal(Object handler, UnderTowConnectorInfo connectorInfo) {
        System.out.println("don't resolve class : " + handler.getClass().getName());
    }
}
