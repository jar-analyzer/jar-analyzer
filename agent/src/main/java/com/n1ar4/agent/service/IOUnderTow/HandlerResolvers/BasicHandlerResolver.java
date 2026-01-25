package com.n1ar4.agent.service.IOUnderTow.HandlerResolvers;

import com.n1ar4.agent.service.BasicFrameworkBaseInfo;
import com.n1ar4.agent.service.IOUnderTow.urlInfo.UnderTowConnectorInfo;
import com.n1ar4.agent.dto.SourceResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class BasicHandlerResolver {
    protected Object nowTurnHandler;
    protected ArrayList<SourceResult> sourceResults;
    protected HashMap<String, ArrayList<BasicFrameworkBaseInfo>> frameworkInstances;

    public BasicHandlerResolver() {
        this.sourceResults = new ArrayList<SourceResult>();
        this.frameworkInstances = new HashMap<String, ArrayList<BasicFrameworkBaseInfo>>();
        this.nowTurnHandler = null;
    }

    protected abstract void resolverInternal(Object handler, UnderTowConnectorInfo connectorInfo);

    public ArrayList<SourceResult> resolve(Object handler, UnderTowConnectorInfo connectorInfo) {
        sourceResults.clear();
        frameworkInstances.clear();
        nowTurnHandler = handler;
        this.resolverInternal(handler, connectorInfo);
        return sourceResults;
    }

    public HashMap<String, ArrayList<BasicFrameworkBaseInfo>> getFrameworkInstances() {
        return frameworkInstances;
    }

    public List<Object> getNextHandlers() {
        return Arrays.asList();
    }
}
