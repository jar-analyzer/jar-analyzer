package com.n1ar4.agent.service.IOUnderTow;

import com.n1ar4.agent.service.BasicFrameworkBaseInfo;
import com.n1ar4.agent.service.IOUnderTow.urlInfo.UnderTowEndpointUrlInfo;
import com.n1ar4.agent.dto.UrlInfo;

import java.util.ArrayList;

public class UndertowFrameworInfo extends BasicFrameworkBaseInfo {
    private final Object instance;
    private final UnderTowEndpointUrlInfo patternInfo;

    public UndertowFrameworInfo(Object instance, UnderTowEndpointUrlInfo patternInfo) {
        this.instance = instance;
        this.patternInfo = patternInfo;
    }
    @Override
    public Object getInstance() {
        return instance;
    }

    @Override
    public ArrayList<UrlInfo> getUrlInfos() {
        return patternInfo.toUrlInfos();
    }

    @Override
    public String getRequestUrl() {
        return patternInfo.toReqUrl();
    }
}
