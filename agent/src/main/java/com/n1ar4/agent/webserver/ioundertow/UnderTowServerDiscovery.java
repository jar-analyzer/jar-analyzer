package com.n1ar4.agent.webserver.ioundertow;

import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.util.FrameworkUtils;
import com.n1ar4.agent.util.ReflectUtils;
import com.n1ar4.agent.webserver.ServerDiscovery;
import com.n1ar4.agent.webserver.ioundertow.handler.BasicHandlerResolver;
import com.n1ar4.agent.webserver.ioundertow.handler.HandlerMap;
import com.n1ar4.agent.webserver.ioundertow.urlInfo.UnderTowConnectorInfo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnderTowServerDiscovery extends ServerDiscovery {
    public UnderTowServerDiscovery(String serverContextClassPath) {
        super(serverContextClassPath);
    }

    @Override
    protected ArrayList<SourceResult> getServerSourceInternal(Object[] instances) {
        if (instances == null || instances.length == 0) {
            return null;
        }
        Object server = instances[0];
        ArrayList<SourceResult> sourceResults = new ArrayList<SourceResult>();
        List<Object> listenerInfos = (List<Object>) ReflectUtils.callMethod(server, "getListenerInfo");
        if (listenerInfos == null) {
            return null;
        }
        UnderTowConnectorInfo connectorInfo = new UnderTowConnectorInfo();
        for (Object listenerInfo : listenerInfos) {
            InetSocketAddress address = (InetSocketAddress) ReflectUtils.getDeclaredField(listenerInfo, "address");
            int port = address.getPort();
            String protocol = (String) ReflectUtils.getDeclaredField(listenerInfo, "protcol");
            String host = address.getAddress().getHostAddress();
            connectorInfo.AddConnector(
                    host, String.valueOf(port), protocol);
        }
        Object rootHandler = ReflectUtils.getDeclaredField(server, "rootHandler");
        if (rootHandler == null) {
            return sourceResults;
        }
        parseHandler(rootHandler, sourceResults, connectorInfo);

        return sourceResults;
    }

    public void parseHandler(Object instance, ArrayList<SourceResult> sourceResults, UnderTowConnectorInfo connectorInfo) {
        List<Object> handlers = new ArrayList<Object>(Arrays.asList(instance));
        List<Object> nextHandlers = new ArrayList<Object>();
        while (handlers.size() > 0) {
            for (Object handler : handlers) {
                BasicHandlerResolver handlerResolver = HandlerMap.getHandlerResolverByInstance(handler);
                sourceResults.addAll(handlerResolver.resolve(handler, connectorInfo));
                FrameworkUtils.MergeFrameworkBaseInfoHashMap(this.frameworkInstances, handlerResolver.getFrameworkInstances());
                nextHandlers.addAll(handlerResolver.getNextHandlers());
            }
            handlers.clear();
            handlers.addAll(nextHandlers);
            nextHandlers.clear();
        }
    }
}
