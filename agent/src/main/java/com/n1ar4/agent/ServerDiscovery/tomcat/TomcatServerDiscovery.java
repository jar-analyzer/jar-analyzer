package com.n1ar4.agent.ServerDiscovery.tomcat;

import com.n1ar4.agent.ServerDiscovery.ServerDiscovery;
import com.n1ar4.agent.sourceResult.SourceResult;
import com.n1ar4.agent.util.ReflectUtils;

import java.util.ArrayList;

public class TomcatServerDiscovery extends ServerDiscovery {
    public TomcatServerDiscovery(String serverClass) {
        super(serverClass);
    }

    @Override
    protected ArrayList<SourceResult> getServerSourceInternal(Object[] instances) {
        Object standardServer = instances[0];
        Object[] services = (Object[]) ReflectUtils.getDeclaredField(standardServer, "services");
        if (services != null) {
            ArrayList<SourceResult> sourceResults = new ArrayList<SourceResult>();
            for (Object service : services) {
                TomcatServiceDiscovery tomcatServiceSourceDiscovery = new TomcatServiceDiscovery(service);
                sourceResults.addAll(tomcatServiceSourceDiscovery.getSourceResults());
            }
            return sourceResults;
        }
        return null;
    }
}
