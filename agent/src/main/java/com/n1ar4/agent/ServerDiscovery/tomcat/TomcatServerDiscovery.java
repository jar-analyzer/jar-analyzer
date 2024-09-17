package com.n1ar4.agent.ServerDiscovery.tomcat;

import com.n1ar4.agent.ServerDiscovery.ServerDiscovery;
import com.n1ar4.agent.sourceResult.SourceResult;
import com.n1ar4.agent.util.ReflectUtils;
import com.n1ar4.agent.util.ThreadUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class TomcatServerDiscovery extends ServerDiscovery {
    public TomcatServerDiscovery(String serverClass) {
        super(serverClass);
    }

    @Override
    public boolean CanLoad() {
        Thread[] threads = ThreadUtils.getThreads();

        for (Thread thread : threads) {
            if(thread.getName().contains("ContainerBackgroundProcessor")){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object[] getLoadedClasses() {
        Thread[] threads = ThreadUtils.getThreads();

        HashSet<Object> servers = new HashSet<>();
        for (Thread thread : threads) {
            if(thread.getName().contains("ContainerBackgroundProcessor") == false)
                continue;
            Object target = ReflectUtils.getDeclaredField(thread , "target");
            if(target == null)
                continue;

            Object target_this = ReflectUtils.getDeclaredField(target , "this$0");
            if(target == null)
                continue;

            Object service = ReflectUtils.getDeclaredField(target_this , "service");
            if(service == null)
                continue;

            Object server = ReflectUtils.getDeclaredField(service , "server");
            if(server == null)
                continue;
            servers.add(server);
            System.out.println("find server obj from thread : " + server.getClass().getName());
        }
        if(servers.size() != 0){
            return servers.toArray();
        }else {
            return null;
        }
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
