package com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo;

import com.n1ar4.agent.sourceResult.UrlInfo;

import java.util.ArrayList;

public class ServiceInfo {
    public ArrayList<UrlInfo> connectorList;
    public String defaultHost;

    public ServiceInfo(){
        this(new ArrayList<UrlInfo>() , "");
    }

    public ServiceInfo(ArrayList<UrlInfo> connectorList){
        this(connectorList , "");
    }

    public ServiceInfo(ArrayList<UrlInfo> connectorList , String defaultHost){
        this.connectorList = connectorList;
        this.defaultHost = defaultHost;
    }

    public ArrayList<UrlInfo> getConnectorList () {
        return connectorList;
    }
}
