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

import com.n1ar4.agent.service.tomcat.TomcatBasicServerDiscovery;
import com.n1ar4.agent.service.IOUnderTow.UnderTowServerDiscovery;
public enum ServerDiscoveryType {
    Tomcat(new TomcatBasicServerDiscovery("org.apache.catalina.core.StandardServer")),
    IOUnderTow(new UnderTowServerDiscovery("io.undertow.Undertow")),
    ;
    private BasicServerDiscovery serverDiscovery;

    private ServerDiscoveryType(BasicServerDiscovery basicServerDiscovery) {
        this.serverDiscovery = basicServerDiscovery;
    }

    public BasicServerDiscovery getServerDiscovery() {
        return serverDiscovery;
    }
}
