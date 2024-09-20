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
