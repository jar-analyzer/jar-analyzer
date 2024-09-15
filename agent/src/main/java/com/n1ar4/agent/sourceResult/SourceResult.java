package com.n1ar4.agent.sourceResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class SourceResult implements Serializable, Comparable<SourceResult> {
    public SourceResultType type;
    public String name;
    public String sourceClass;
    public String methodInfo;
    public ArrayList<UrlInfo> urlInfos = null;
    public ArrayList<String> description = null;

    public SourceResult() {

    }

    public SourceResult(SourceResultType type, String name, String sourceClass, ArrayList<UrlInfo> urlInfos) {
        this.type = type;
        this.name = name;
        this.sourceClass = sourceClass;
        this.urlInfos = urlInfos;
        this.methodInfo = null;
        this.description = null;
    }

    public SourceResult(SourceResultType type, String name, String sourceClass, String methodInfo, ArrayList<UrlInfo> urlInfos) {
        this(type, name, sourceClass, urlInfos);
        this.methodInfo = methodInfo;
    }

    public SourceResult(SourceResultType type, String name, String sourceClass, ArrayList<UrlInfo> urlInfos, ArrayList<String> description) {
        this(type, name, sourceClass, urlInfos);
        if (description != null && !description.isEmpty())
            this.description = description;
    }

    public SourceResult(SourceResultType type, String name, String sourceClass, String methodInfo, ArrayList<UrlInfo> urlInfos, ArrayList<String> description) {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceResult targetSourceResult = (SourceResult) o;
        if (targetSourceResult.type.equals(this.type) == false)
            return false;
        if(targetSourceResult.name == null){
            if(this.name != null) {
                return false;
            }
        }else if (targetSourceResult.name.equals(this.name) == false){
            return false;
        }

        if (targetSourceResult.sourceClass.equals(this.sourceClass) == false)
            return false;
        if (targetSourceResult.methodInfo != null) {
            if (targetSourceResult.methodInfo.equals(this.methodInfo) == false) {
                return false;
            }
        } else {
            if (this.methodInfo != null)
                return false;
        }
        if (targetSourceResult.getUrlInfos().toString().equals(this.getUrlInfos().toString()) == false)
            return false;
        return true;
    }

    private int hashResult = -1;

    private int type_hash(Object data) {
        if (data == null)
            return 0;
        else
            return data.hashCode();
    }

    public int hashCode() {
        if (hashResult == -1) {
            hashResult = 1;
            hashResult = 31 * hashResult + type_hash(type);
            hashResult = 31 * hashResult + type_hash(name);

            hashResult = 31 * hashResult + type_hash(sourceClass);
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
            return Arrays.toString(new ArrayList[]{this.urlInfos}).compareTo(Arrays.toString(new ArrayList[]{o.getUrlInfos()}));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Source Type : " + this.getType().toString() + "\n");
        sb.append("\t Source Name : " + this.getName() + "\n");
        sb.append("\t Source Class : " + this.getSourceClass() + "\n");
        if(this.urlInfos != null && this.urlInfos.size() > 0){
            sb.append("\t Source UrlInfo : \n");
            for (UrlInfo urlInfo : this.urlInfos) {
                sb.append("\t\t Url : " + urlInfo.getUrl() + "\n");
                if(urlInfo.getDescrition().equals(""))
                    continue;
                for (String oneLineDesc : urlInfo.descrition.split("|")) {
                    sb.append("\t\t\t desc : " + oneLineDesc + "\n");
                }
            }
        }
        ArrayList<String> descripton = this.getDescription();
        if ( descripton!= null && descripton.size() > 0) {
            sb.append("\t Source Description : \n");
            for (String desc : descripton) {
                sb.append("\t\t " + desc + "\n");
            }
        }
        return sb.toString();
    }
}