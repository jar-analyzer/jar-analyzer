package com.n1ar4.agent.dto;

import java.util.ArrayList;

public class UrlInfoAndDescMapValue {
    public String tag;
    public ArrayList<UrlInfo> urlInfos;
    public ArrayList<String> desc;

    public UrlInfoAndDescMapValue(String tag){
        this.tag = tag;
        this.urlInfos = new ArrayList<>();
        this.desc = new ArrayList<>();
    }
}
