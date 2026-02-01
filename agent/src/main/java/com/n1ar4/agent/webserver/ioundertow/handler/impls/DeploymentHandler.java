package com.n1ar4.agent.webserver.ioundertow.handler.impls;

import com.n1ar4.agent.util.ReflectUtils;
import com.n1ar4.agent.webserver.ioundertow.handler.BasicHandlerResolver;
import com.n1ar4.agent.webserver.ioundertow.urlInfo.UnderTowConnectorInfo;

import java.util.Arrays;
import java.util.List;

public class DeploymentHandler extends BasicHandlerResolver {

    private Object objHandler;

    @Override
    protected void resolverInternal(Object handler, UnderTowConnectorInfo connectorInfo) {
        this.objHandler = handler;
    }

    @Override
    public List<Object> getNextHandlers() {
        if (this.objHandler != null) {
            return Arrays.asList(ReflectUtils.getDeclaredField(this.objHandler, "handler"));
        } else {
            return Arrays.asList();
        }

    }
}
