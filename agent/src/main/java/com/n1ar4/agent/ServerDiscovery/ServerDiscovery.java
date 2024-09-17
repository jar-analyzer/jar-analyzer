package com.n1ar4.agent.ServerDiscovery;

import com.n1ar4.agent.sourceResult.SourceResult;

import java.util.ArrayList;

public abstract class ServerDiscovery {
    protected String serverClass;

    public ServerDiscovery(String serverClass) {
        this.serverClass = serverClass;
    }

    public abstract boolean CanLoad();
//    public boolean CanLoad(VmTool vmTool, Instrumentation inst) {
//        ArrayList<Class<?>> matchedClasses = new ArrayList<Class<?>>(SearchUtils.searchClassOnly(inst, serverClass, false, null));
//        if (matchedClasses.size() == 0)
//            return false;
//        Object[] instances = vmTool.getInstances(matchedClasses.get(0));
//        return instances.length > 0;
//    }

    public abstract Object[] getLoadedClasses();
//    public Object[] getLoadedClasses(VmTool vmTool, Instrumentation inst) {
//        ArrayList<Class<?>> matchedClasses = new ArrayList<Class<?>>(SearchUtils.searchClassOnly(inst, serverClass, false, null));
//        Class<?> contextClass = matchedClasses.get(0);
//        Object[] instances = vmTool.getInstances(contextClass);
//        return instances;
//    }

    public ArrayList<SourceResult> getServerSources() {
        Object[] instaces = getLoadedClasses();
        if (instaces == null)
            return new ArrayList<SourceResult>();
        ArrayList<SourceResult> sourceResults = this.getServerSourceInternal(instaces);
        return sourceResults != null ? sourceResults : new ArrayList<SourceResult>();
    }

    protected abstract ArrayList<SourceResult> getServerSourceInternal(Object[] instances);

}
