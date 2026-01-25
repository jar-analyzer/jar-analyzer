package com.n1ar4.agent.service.IOUnderTow.urlInfo;


import com.n1ar4.agent.dto.UrlInfo;

import java.util.ArrayList;
import java.util.HashSet;

public class UnderTowEndpointUrlInfo {
    public HashSet<String> patterns;
    public UnderTowConnectorInfo connectorInfo;
    public final String contextPath;
    private ArrayList<UrlInfo> patternUrlInfos;
    public ArrayList<String> desc;


    public UnderTowEndpointUrlInfo(UnderTowConnectorInfo connectorInfo, String contextPath) {
        this.patterns = new HashSet<String>();
        this.connectorInfo = connectorInfo;
        this.contextPath = contextPath;
        this.patternUrlInfos = null;
        this.desc = new ArrayList<String>();
    }

    public ArrayList<UrlInfo> toUrlInfos() {
        if (patternUrlInfos != null)
            return this.patternUrlInfos;
        ArrayList<UrlInfo> connectorUrlList = connectorInfo.toUrlInfos();
        ArrayList<UrlInfo> patternUrlInfos = new ArrayList<UrlInfo>();

        for (UrlInfo connectorUrlInfo : connectorUrlList) {
            if (patterns.isEmpty()) {
                UrlInfo newUrlInfo = new UrlInfo(connectorUrlInfo);
                newUrlInfo.appendUrl(contextPath);
                patternUrlInfos.add(newUrlInfo);
            } else {
                for (String pattern : patterns) {
                    UrlInfo newUrlInfo = new UrlInfo(connectorUrlInfo);
                    newUrlInfo.appendUrl(contextPath);
                    newUrlInfo.appendUrl(pattern);
                    patternUrlInfos.add(newUrlInfo);
                }
            }
//            if (desc.size() > 0) {
//                for (String descItem : desc) {
//                    for (UrlInfo patternUrlInfo : patternUrlInfos) {
//                        patternUrlInfo.appendDescription(descItem);
//                    }
//                }
//            }
        }
        this.patternUrlInfos = patternUrlInfos;
        return patternUrlInfos;
    }

    public String toReqUrl() {
        for (UrlInfo patternUrlInfo : toUrlInfos()) {
            if (patternUrlInfo.getUrl().startsWith("http"))
                return patternUrlInfo.getUrl();
        }
        return toUrlInfos().get(0).getUrl();
    }
}
