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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings("unused")
public class SourceResult implements Serializable, Comparable<SourceResult> {
    public SourceResultType type;
    public String name;
    public String sourceClass;
    public String methodInfo;
    public ArrayList<UrlInfo> urlInfos = null;
    public ArrayList<String> description = null;
    public static String SourceResultTag = "sourceTag:";

    public SourceResult() {
    }

    public SourceResult(SourceResultType type,
                        String name,
                        String sourceClass,
                        ArrayList<UrlInfo> urlInfos) {
        this.type = type;
        this.name = name;
        this.sourceClass = sourceClass;
        this.urlInfos = urlInfos;
        this.methodInfo = null;
        this.description = null;
    }

    public SourceResult(SourceResultType type,
                        String name,
                        String sourceClass,
                        String methodInfo,
                        ArrayList<UrlInfo> urlInfos) {
        this(type, name, sourceClass, urlInfos);
        this.methodInfo = methodInfo;
    }

    public SourceResult(SourceResultType type,
                        String name,
                        String sourceClass,
                        ArrayList<UrlInfo> urlInfos,
                        ArrayList<String> description) {
        this(type, name, sourceClass, urlInfos);
        if (description != null && !description.isEmpty())
            this.description = description;
    }

    public SourceResult(SourceResultType type,
                        String name,
                        String sourceClass,
                        String methodInfo,
                        ArrayList<UrlInfo> urlInfos,
                        ArrayList<String> description) {
        this(type, name, sourceClass, urlInfos, description);
        this.methodInfo = methodInfo;
    }

    public SourceResultType getType() {
        return type;
    }

    public void setType(SourceResultType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(String sourceClass) {
        this.sourceClass = sourceClass;
    }

    public String getMethodInfo() {
        return methodInfo;
    }

    public void setMethodInfo(String methodInfo) {
        this.methodInfo = methodInfo;
    }

    public ArrayList<UrlInfo> getUrlInfos() {
        return urlInfos;
    }

    public void setUrlInfos(ArrayList<UrlInfo> urlInfos) {
        this.urlInfos = urlInfos;
    }

    public ArrayList<String> getDescription() {
        return description;
    }

    public void setDescription(ArrayList<String> description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SourceResult targetSourceResult = (SourceResult) o;
        if (!targetSourceResult.type.equals(this.type)) {
            return false;
        }
        if (targetSourceResult.name == null) {
            if (this.name != null) {
                return false;
            }
        } else if (!targetSourceResult.name.equals(this.name)) {
            return false;
        }
        if (!targetSourceResult.sourceClass.equals(this.sourceClass)) {
            return false;
        }
        if (targetSourceResult.methodInfo != null) {
            if (!targetSourceResult.methodInfo.equals(this.methodInfo)) {
                return false;
            }
        } else {
            if (this.methodInfo != null) {
                return false;
            }
        }
        return targetSourceResult.getUrlInfos().toString().equals(this.getUrlInfos().toString());
    }

    private int hashResult = -1;

    private int typeHash(Object data) {
        if (data == null) {
            return 0;
        } else {
            return data.hashCode();
        }
    }

    public int hashCode() {
        if (hashResult == -1) {
            hashResult = 1;
            hashResult = 31 * hashResult + typeHash(type);
            hashResult = 31 * hashResult + typeHash(name);
            hashResult = 31 * hashResult + typeHash(sourceClass);
            hashResult = 31 * hashResult + (methodInfo == null ? "".hashCode() : methodInfo.hashCode());
            hashResult = 31 * hashResult + (urlInfos == null ? 0 : Arrays.hashCode(getUrlInfos().toArray()));
            hashResult = 31 * hashResult + (description == null ? 0 : Arrays.hashCode(getDescription().toArray()));
        }
        return hashResult;
    }

    @Override
    public int compareTo(SourceResult o) {
        if (this.type.ordinal() != o.type.ordinal()) {
            return this.type.ordinal() - o.type.ordinal();
        } else {
            return Arrays.toString(new ArrayList[]{this.urlInfos}).compareTo(
                    Arrays.toString(new ArrayList[]{o.getUrlInfos()}));
        }
    }

    public HashMap<String, UrlInfoAndDescMapValue> getSourceTagMapForUrlInfosAndDesc() {
        HashMap<String, UrlInfoAndDescMapValue> tagHashMap = new HashMap<>();
        ArrayList<String> descList = this.getDescription();
        String nowTag = "";
        for (String oneLineDesc : descList) {
            if (oneLineDesc.startsWith(SourceResultTag)) {
                String tag = oneLineDesc.split(SourceResultTag)[1];
                tagHashMap.put(tag, new UrlInfoAndDescMapValue(tag));
                nowTag = tag;
            } else {
                UrlInfoAndDescMapValue urlInfoAndDescMapValue = tagHashMap.get(nowTag);
                if (urlInfoAndDescMapValue == null) {
                    System.out.println("[-] error out : not found target tag : " + nowTag);
                    continue;
                }
                urlInfoAndDescMapValue.desc.add(oneLineDesc);
            }
        }

        for (UrlInfo urlInfo : this.getUrlInfos()) {
            String[] descriptionList = urlInfo.getDescriptionList();
            String lastUrlDesc = descriptionList[descriptionList.length - 1];
            if (!lastUrlDesc.startsWith(SourceResultTag)) {
                System.out.println("[-] not found tag in url : " + urlInfo.getUrl());
                continue;
            }
            String tag = lastUrlDesc.split(SourceResultTag)[1];
            UrlInfoAndDescMapValue urlInfoAndDescMapValue = tagHashMap.get(tag);
            if (urlInfoAndDescMapValue == null) {
                System.out.println("[-] error out : not found target tag in url list: " + nowTag);
                continue;
            }
            urlInfoAndDescMapValue.urlInfos.add(urlInfo);
        }

        return tagHashMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Source Type : ").append(this.getType().toString()).append("\n");
        sb.append("\t Source Name : ").append(this.getName()).append("\n");
        sb.append("\t Source Class : ").append(this.getSourceClass()).append("\n");
        HashMap<String, UrlInfoAndDescMapValue> sourceTagMapForUrlInfosAndDesc = getSourceTagMapForUrlInfosAndDesc();
        Collection<UrlInfoAndDescMapValue> values = sourceTagMapForUrlInfosAndDesc.values();
        for (UrlInfoAndDescMapValue value : values) {
            sb.append("\t Source Result Tag : ").append(value.tag).append("\n");
            if (value.urlInfos != null && !value.urlInfos.isEmpty()) {
                sb.append("\t\t Source UrlInfo : \n");
                for (UrlInfo urlInfo : value.urlInfos) {
                    sb.append("\t\t\t Url : ").append(urlInfo.getUrl()).append("\n");
                    if (urlInfo.getDescription().isEmpty()) {
                        continue;
                    }
                    for (String oneLineDesc : urlInfo.getDescriptionList()) {
                        if (!oneLineDesc.startsWith(SourceResultTag))
                            sb.append("\t\t\t\t desc : ").append(oneLineDesc.trim()).append("\n");
                    }
                }
            }
            ArrayList<String> description = value.desc;
            if (description != null && !description.isEmpty()) {
                sb.append("\t\t Source Description : \n");
                for (String desc : description) {
                    sb.append("\t\t\t ").append(desc).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public String generateUrlInfo() {
        StringBuilder sb = new StringBuilder();
        if (this.urlInfos != null && !this.urlInfos.isEmpty()) {
            for (UrlInfo urlInfo : this.urlInfos) {
                sb.append(urlInfo.getUrl());
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}