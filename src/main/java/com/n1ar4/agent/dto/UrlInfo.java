/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.dto;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class UrlInfo implements Serializable {
    public String url;
    public String description;
    private static final String urlInfoDescTag = "^&*$#@";
    private static final String urlInfoDescSplitTag = "\\^&\\*\\$#@";

    public UrlInfo(String url) {
        this(url, "");
    }

    public UrlInfo(UrlInfo base) {
        this(base.url, base.description);
    }

    public UrlInfo(String url, String description) {
        this.url = url;
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void appendRawUrl(String append) {
        this.url += append;
    }

    private String getLastUri() {
        return this.url.substring(this.url.lastIndexOf("/"));
    }

    private boolean appendRegexPattern(String urlPattern) {
        String lastUri = getLastUri();
        if (lastUri.startsWith("/*")) {
            String baseRegexUrl = this.url.substring(0, this.url.lastIndexOf(lastUri));
            String ext = "";
            if (lastUri.length() > 2) {
                ext = lastUri.substring(2);
            }
            this.appendDescription(String.format("baseUrl : %s | urlPattern : %s", this.url, urlPattern));
            this.setUrl(String.format("%s%s%s", baseRegexUrl, urlPattern, ext));
            return true;
        }
        return false;
    }

    public void appendUrl(String urlPattern) {
        if (appendRegexPattern(urlPattern))
            return;
        if (this.url.endsWith("/")) {
            if (urlPattern.startsWith("/")) {
                urlPattern = urlPattern.substring(1);
            }
        }
        if(!urlPattern.startsWith("/") && !this.url.endsWith("/")){
            urlPattern = "/" + urlPattern;
        }

        if(this.url.endsWith("*") && urlPattern.startsWith("*")){
            urlPattern = urlPattern.substring(1);
        }
        this.appendRawUrl(urlPattern);
    }

    public String getDescription() {
        if(description == null){
            return "";
        }
        return description;
    }

    public String[] getDescriptionList() {
        return description.split(urlInfoDescSplitTag);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void appendDescription(String append) {
        StringBuilder appendDesc = new StringBuilder();
        if(!this.description.equals("")){
            appendDesc.append(this.description);
            appendDesc.append(urlInfoDescTag);
        }
        appendDesc.append(append);
        this.description = appendDesc.toString();
    }

    public static ArrayList<UrlInfo> UrlInfoCopy(ArrayList<UrlInfo> urlInfos){
        ArrayList<UrlInfo> newUrlInfos = new ArrayList<UrlInfo>();
        for(UrlInfo urlInfo : urlInfos){
            newUrlInfos.add(new UrlInfo(urlInfo));
        }
        return newUrlInfos;
    }
}
