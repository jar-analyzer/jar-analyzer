/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.dto;

import java.io.Serializable;

@SuppressWarnings("unused")
public class UrlInfo implements Serializable {
    public String url;
    public String description;
    private static final String urlInfoDescTag = "^&*$#@";
    private static final String urlInfoDescSplitTag = "\\^&\\*\\$#@";

    public UrlInfo(String url) {
        this(url, "");
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

    public void appendUrl(String urlPattern) {
        if (!urlPattern.startsWith("/")) {
            url += "/";
        }
        this.appendRawUrl(urlPattern);
    }

    public String getDescription() {
        return description;
    }

    public String[] getDescriptionList() {
        return description.split(urlInfoDescSplitTag);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void appendDescription(String append) {
        this.description += urlInfoDescTag + append;
    }
}
