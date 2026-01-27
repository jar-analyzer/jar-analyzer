package com.n1ar4.agent.webserver.ioundertow.handler.impls;

import com.n1ar4.agent.webserver.ioundertow.handler.BasicHandlerResolver;
import com.n1ar4.agent.webserver.ioundertow.urlInfo.UnderTowConnectorInfo;

public class DefaultHandler extends BasicHandlerResolver {
    @Override
    protected void resolverInternal(Object handler, UnderTowConnectorInfo connectorInfo) {
        System.out.println("don't resolve class : " + handler.getClass().getName());
    }
}
