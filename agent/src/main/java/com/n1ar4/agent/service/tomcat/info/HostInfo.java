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

public class HostInfo {
    public String hostName;
    public ServiceInfo serviceUrlInfo;
    private ArrayList<UrlInfo> hostUrlInfoList;

    public HostInfo(ServiceInfo serviceUrlInfo, String hostName) {
        this.serviceUrlInfo = serviceUrlInfo;
        this.hostName = hostName;
        this.hostUrlInfoList = null;
    }

    public boolean isDefaultHost() {
        return this.serviceUrlInfo.defaultHost.equals(hostName);
    }


    public ArrayList<UrlInfo> getHostUrlInfoList() {
        if (hostUrlInfoList == null) {
            this.hostUrlInfoList = new ArrayList<>();
            for (UrlInfo serviceInfo : this.serviceUrlInfo.getConnectorList()) {
                String nowHostDescription = String.format("hostname:%s,isDefaultHost:%s", hostName, isDefaultHost());
                UrlInfo nowHostUrlInfo = new UrlInfo(serviceInfo.getUrl(), serviceInfo.getDescription());
                nowHostUrlInfo.appendDescription(nowHostDescription);
                this.hostUrlInfoList.add(nowHostUrlInfo);
            }
        }
        return this.hostUrlInfoList;
    }
}
