package com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo;

import com.n1ar4.agent.sourceResult.UrlInfo;

import java.util.ArrayList;

public class ContextInfo {
    public HostInfo parentHostInfo;
    public String ContextUrlBase;
    private ArrayList<UrlInfo> contextUrlInfoList;
    public ContextInfo(HostInfo hostUrlInfo , String ContextUrlBase) {
        this.ContextUrlBase = ContextUrlBase;
        this.parentHostInfo = hostUrlInfo;
        this.contextUrlInfoList = null;
    }

    public ContextInfo(HostInfo tomcatHostUrlInfo) {
        this(tomcatHostUrlInfo ,"");
    }

    public ArrayList<UrlInfo> getContextUrlInfoList(){
        if(contextUrlInfoList == null){
            this.contextUrlInfoList = new ArrayList<>();
            for (UrlInfo hostUrlInfo : this.parentHostInfo.getHostUrlInfoList()) {
                UrlInfo nowContextUrlInfo = new UrlInfo(hostUrlInfo.getUrl() , hostUrlInfo.getDescrition());
                nowContextUrlInfo.appendUrl(this.ContextUrlBase);
                this.contextUrlInfoList.add(nowContextUrlInfo);
            }
        }
        return this.contextUrlInfoList;
    }

    @Override
    public String toString() {
        return String.format("%s,urlPattern: %s",this.parentHostInfo.toString() , ContextUrlBase);
    }


}
