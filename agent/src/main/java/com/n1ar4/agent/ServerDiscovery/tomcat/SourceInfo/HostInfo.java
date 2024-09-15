package com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo;

import com.n1ar4.agent.sourceResult.UrlInfo;

import java.util.ArrayList;

public class HostInfo {
    public String hostName;
    public ServiceInfo serviceUrlInfo;
    private ArrayList<UrlInfo> hostUrlInfoList;
    public HostInfo(ServiceInfo serviceUrlInfo , String hostName) {
        this.serviceUrlInfo = serviceUrlInfo;
        this.hostName = hostName;
        this.hostUrlInfoList = null;
    }

    public boolean isDefaultHost(){
        return this.serviceUrlInfo.defaultHost.equals(hostName);
    }


    public ArrayList<UrlInfo> getHostUrlInfoList(){
        if(hostUrlInfoList == null){
            this.hostUrlInfoList = new ArrayList<>();
            for (UrlInfo serviceInfo : this.serviceUrlInfo.getConnectorList()) {
                String nowHostDescription = String.format("hostname:%s,isDefaultHost:%s" , hostName, String.valueOf(isDefaultHost()));
                UrlInfo nowHostUrlInfo = new UrlInfo(serviceInfo.getUrl(), serviceInfo.getDescrition());
                nowHostUrlInfo.appendDescrition(nowHostDescription);
                this.hostUrlInfoList.add(nowHostUrlInfo);
            }
        }
        return this.hostUrlInfoList;
    }
}
