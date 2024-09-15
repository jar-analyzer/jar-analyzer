package com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo;

import com.n1ar4.agent.sourceResult.UrlInfo;

import java.util.ArrayList;

public class EndPointUrlInfo {
    public ContextInfo contextUrlInfo;
    public ArrayList<String> urlPatterns;

    public EndPointUrlInfo(ContextInfo contextUrlInfo) {
        this.urlPatterns = new ArrayList<String>();
        this.contextUrlInfo = contextUrlInfo;
    }

    public ArrayList<UrlInfo> toUrlInfos() {
        ArrayList<UrlInfo> urlInfos = new ArrayList<>();
        for (UrlInfo contextUrlInfo : this.contextUrlInfo.getContextUrlInfoList()) {
            for (String urlPattern : urlPatterns) {
                UrlInfo nowUrlInfo = new UrlInfo(contextUrlInfo.getUrl(), contextUrlInfo.getDescrition());
                nowUrlInfo.appendUrl(urlPattern);
                urlInfos.add(nowUrlInfo);
            }
        }
        return urlInfos;
    }

}
