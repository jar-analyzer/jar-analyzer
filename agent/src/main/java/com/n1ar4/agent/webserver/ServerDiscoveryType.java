/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.webserver;

import com.n1ar4.agent.webserver.ioundertow.UnderTowServerDiscovery;
import com.n1ar4.agent.webserver.tomcat.TomcatServerDiscovery;

public enum ServerDiscoveryType {
    Tomcat(new TomcatServerDiscovery("org.apache.catalina.core.StandardServer")),
    IOUnderTow(new UnderTowServerDiscovery("io.undertow.Undertow")),
    ;
    private ServerDiscovery serverDiscovery;

    private ServerDiscoveryType(ServerDiscovery basicServerDiscovery) {
        this.serverDiscovery = basicServerDiscovery;
    }

    public ServerDiscovery getServerDiscovery() {
        return serverDiscovery;
    }
}
