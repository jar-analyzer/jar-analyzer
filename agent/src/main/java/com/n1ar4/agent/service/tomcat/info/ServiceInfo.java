/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.service.tomcat.info;

import com.n1ar4.agent.dto.UrlInfo;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class ServiceInfo {
    public ArrayList<UrlInfo> connectorList;
    public String defaultHost;

    public ServiceInfo() {
        this(new ArrayList<>(), "");
    }

    public ServiceInfo(ArrayList<UrlInfo> connectorList) {
        this(connectorList, "");
    }

    public ServiceInfo(ArrayList<UrlInfo> connectorList, String defaultHost) {
        this.connectorList = connectorList;
        this.defaultHost = defaultHost;
    }

    public ArrayList<UrlInfo> getConnectorList() {
        return connectorList;
    }
}
