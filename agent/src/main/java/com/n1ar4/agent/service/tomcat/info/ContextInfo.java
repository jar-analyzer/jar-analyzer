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

public class ContextInfo {
    public HostInfo parentHostInfo;
    public String ContextUrlBase;
    private ArrayList<UrlInfo> contextUrlInfoList;

    public ContextInfo(HostInfo hostUrlInfo, String ContextUrlBase) {
        this.ContextUrlBase = ContextUrlBase;
        this.parentHostInfo = hostUrlInfo;
        this.contextUrlInfoList = null;
    }

    public ContextInfo(HostInfo tomcatHostUrlInfo) {
        this(tomcatHostUrlInfo, "");
    }

    public ArrayList<UrlInfo> getContextUrlInfoList() {
        if (contextUrlInfoList == null) {
            this.contextUrlInfoList = new ArrayList<>();
            for (UrlInfo hostUrlInfo : this.parentHostInfo.getHostUrlInfoList()) {
                UrlInfo nowContextUrlInfo = new UrlInfo(hostUrlInfo.getUrl(), hostUrlInfo.getDescription());
                nowContextUrlInfo.appendUrl(this.ContextUrlBase);
                this.contextUrlInfoList.add(nowContextUrlInfo);
            }
        }
        return this.contextUrlInfoList;
    }

    @Override
    public String toString() {
        return String.format("%s,urlPattern: %s", this.parentHostInfo.toString(), ContextUrlBase);
    }
}
