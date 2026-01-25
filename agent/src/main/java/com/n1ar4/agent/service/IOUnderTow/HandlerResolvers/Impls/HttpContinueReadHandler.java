package com.n1ar4.agent.service.IOUnderTow.HandlerResolvers.Impls;

import com.n1ar4.agent.service.IOUnderTow.HandlerResolvers.BasicHandlerResolver;
import com.n1ar4.agent.service.IOUnderTow.urlInfo.UnderTowConnectorInfo;
import com.n1ar4.agent.Utils.ReflectUtils;

import java.util.Arrays;
import java.util.List;

public class HttpContinueReadHandler extends BasicHandlerResolver {
    private Object objHandler;

    @Override
    protected void resolverInternal(Object handler, UnderTowConnectorInfo connectorInfo) {
        this.objHandler = handler;
    }

    @Override
    public List<Object> getNextHandlers() {
        if(this.objHandler != null){
            return Arrays.asList(ReflectUtils.getDeclaredField(this.objHandler , "handler"));
        }else{
            return Arrays.asList();
        }
    }
}
