/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.service;

import com.n1ar4.agent.service.tomcat.TomcatServerDiscovery;

public enum ServerDiscoveryType {
    Tomcat(new TomcatServerDiscovery("org.apache.catalina.core.StandardServer"));
    private final ServerDiscovery serverDiscovery;

    ServerDiscoveryType(ServerDiscovery serverDiscovery) {
        this.serverDiscovery = serverDiscovery;
    }

    public ServerDiscovery getServerDiscovery() {
        return serverDiscovery;
    }
}
