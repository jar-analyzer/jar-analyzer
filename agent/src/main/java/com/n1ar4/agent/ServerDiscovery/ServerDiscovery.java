package com.n1ar4.agent.ServerDiscovery;

import com.n1ar4.agent.sourceResult.SourceResult;

import java.util.ArrayList;

public abstract class ServerDiscovery {
    protected String serverClass;

    public ServerDiscovery(String serverClass) {
        this.serverClass = serverClass;
    }

    public abstract boolean CanLoad();

    public abstract Object[] getLoadedClasses();

    public ArrayList<SourceResult> getServerSources() {
        Object[] instaces = getLoadedClasses();
        if (instaces == null)
            return new ArrayList<SourceResult>();
        ArrayList<SourceResult> sourceResults = this.getServerSourceInternal(instaces);
        return sourceResults != null ? sourceResults : new ArrayList<SourceResult>();
    }

    protected abstract ArrayList<SourceResult> getServerSourceInternal(Object[] instances);

}
