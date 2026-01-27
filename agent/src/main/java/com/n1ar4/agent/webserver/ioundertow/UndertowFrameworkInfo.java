package com.n1ar4.agent.webserver.ioundertow;

import com.n1ar4.agent.webserver.FrameworkBaseInfo;
import com.n1ar4.agent.webserver.ioundertow.urlInfo.UnderTowEndpointUrlInfo;
import com.n1ar4.agent.dto.UrlInfo;

import java.util.ArrayList;

public class UndertowFrameworkInfo extends FrameworkBaseInfo {
    private final Object instance;
    private final UnderTowEndpointUrlInfo patternInfo;

    public UndertowFrameworkInfo(Object instance, UnderTowEndpointUrlInfo patternInfo) {
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
