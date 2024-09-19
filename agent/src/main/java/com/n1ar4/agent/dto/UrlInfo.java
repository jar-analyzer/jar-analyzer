/*
 * MIT License
 *
 * Copyright (c) 2023-2024 4ra1n (Jar Analyzer Team)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
