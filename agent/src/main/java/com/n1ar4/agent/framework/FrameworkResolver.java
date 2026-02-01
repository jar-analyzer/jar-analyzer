package com.n1ar4.agent.framework;

import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.UrlInfo;
import com.n1ar4.agent.webserver.FrameworkBaseInfo;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class FrameworkResolver {
    public String resolverClass;
    protected HashMap<String, ArrayList<FrameworkBaseInfo>> frameworkInstances = new HashMap<String, ArrayList<FrameworkBaseInfo>>();

    public FrameworkResolver(String resolverClass) {
        this.resolverClass = resolverClass;
    }

    public boolean canResolve(String targetClass) {
        return targetClass.equals(resolverClass);
    }

    public HashMap<String, ArrayList<FrameworkBaseInfo>> getFrameworkInstances() {
        return frameworkInstances;
    }

    protected abstract ArrayList<SourceResult> resolveInternal(Object instance, ArrayList<UrlInfo> baseUrlMappings);

    public boolean isKeep() {
        return true;
    }

    public ArrayList<SourceResult> resolve(Object instance, ArrayList<UrlInfo> urlInfos) {
        if (instance == null) {
            return new ArrayList<SourceResult>();
        }
        this.frameworkInstances.clear();
        ArrayList<SourceResult> sourceResults = this.resolveInternal(instance, urlInfos);
        return sourceResults != null ? sourceResults : new ArrayList<SourceResult>();
    }
}
