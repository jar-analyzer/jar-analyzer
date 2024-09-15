package com.n1ar4.agent.sourceResult;

import java.io.Serializable;

public class UrlInfo implements Serializable {
    public String url;
    public String descrition;

    public UrlInfo(String url) {
        this(url , "");
    }

    public UrlInfo(String url, String descrition) {
        this.url = url;
        this.descrition = descrition;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void appendRawUrl(String append){
        this.url += append;
    }

    public void appendUrl(String urlPattern){
        if (urlPattern.startsWith("/") == false)
            url += "/";
        this.appendRawUrl(urlPattern);
    }


    public String getDescrition() {
        return descrition;
    }

    public void setDescrition(String descrition) {
        this.descrition = descrition;
    }

    public void appendDescrition(String append){
        this.descrition += " | " + append;
    }


}
