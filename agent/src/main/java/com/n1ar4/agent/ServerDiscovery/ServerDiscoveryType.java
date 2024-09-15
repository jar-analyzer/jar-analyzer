package com.n1ar4.agent.ServerDiscovery;

import com.n1ar4.agent.ServerDiscovery.tomcat.TomcatServerDiscovery;

public enum ServerDiscoveryType {
    Tomcat(new TomcatServerDiscovery("org.apache.catalina.core.StandardServer"))
    ;
    private ServerDiscovery serverDiscovery;

    private ServerDiscoveryType(ServerDiscovery serverDiscovery){
        this.serverDiscovery = serverDiscovery;
    }

    public ServerDiscovery getServerDiscovery() {
        return serverDiscovery;
    }
}
