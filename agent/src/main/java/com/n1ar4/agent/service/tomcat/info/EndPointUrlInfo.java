/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.service.tomcat.info;

import com.n1ar4.agent.dto.UrlInfo;

import java.util.ArrayList;

public class EndPointUrlInfo {
    public ContextInfo contextUrlInfo;
    public ArrayList<String> urlPatterns;

    public EndPointUrlInfo(ContextInfo contextUrlInfo) {
        this.urlPatterns = new ArrayList<>();
        this.contextUrlInfo = contextUrlInfo;
    }

    public ArrayList<UrlInfo> toUrlInfos() {
        ArrayList<UrlInfo> urlInfos = new ArrayList<>();
        for (UrlInfo contextUrlInfo : this.contextUrlInfo.getContextUrlInfoList()) {
            for (String urlPattern : urlPatterns) {
                UrlInfo nowUrlInfo = new UrlInfo(contextUrlInfo.getUrl(), contextUrlInfo.getDescription());
                nowUrlInfo.appendUrl(urlPattern);
                urlInfos.add(nowUrlInfo);
            }
        }
        return urlInfos;
    }
}
