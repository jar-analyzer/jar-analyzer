package com.n1ar4.agent.webserver.ioundertow.handler;

import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.webserver.FrameworkBaseInfo;
import com.n1ar4.agent.webserver.ioundertow.urlInfo.UnderTowConnectorInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class BasicHandlerResolver {
    protected Object nowTurnHandler;
    protected ArrayList<SourceResult> sourceResults;
    protected HashMap<String, ArrayList<FrameworkBaseInfo>> frameworkInstances;

    public BasicHandlerResolver() {
        this.sourceResults = new ArrayList<SourceResult>();
        this.frameworkInstances = new HashMap<String, ArrayList<FrameworkBaseInfo>>();
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

    public HashMap<String, ArrayList<FrameworkBaseInfo>> getFrameworkInstances() {
        return frameworkInstances;
    }

    public List<Object> getNextHandlers() {
        return Arrays.asList();
    }
}
