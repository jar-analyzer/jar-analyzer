package com.n1ar4.agent.service.IOUnderTow.urlInfo;

import com.n1ar4.agent.dto.UrlInfo;

import java.util.ArrayList;

public class UnderTowConnectorInfo {
    public static class BaseInfo {
        public String address;
        public String port;
        public String protocol;

        public BaseInfo(String address, String port, String protocol) {
            this.address = address;
            this.port = port;
            this.protocol = protocol;
        }
    }

    public ArrayList<BaseInfo> connectors;

    public UnderTowConnectorInfo() {
        this.connectors = new ArrayList<BaseInfo>();
    }
    public void AddConnector(String address, String port, String protocol) {
        connectors.add(new BaseInfo(address, port, protocol));
    }

    public String getConnectorPath(ArrayList<String> particularHosts) {
        if (connectors.isEmpty()) {
            return null;
        }
        BaseInfo connector = connectors.get(0);
        return String.format("%s://%s:%s/",
                connector.protocol,
                particularHosts.isEmpty() ? (connector.address.equals("0.0.0.0") ? "0.0.0.0" : connector.address) : particularHosts.get(0),
                connector.port);
    }


    public ArrayList<UrlInfo> toUrlInfos() {
        ArrayList<UrlInfo> connecterUrlList = new ArrayList<UrlInfo>();
        for (BaseInfo connector : connectors) {
            UrlInfo connectorUrlInfo = new UrlInfo(String.format("%s://%s:%s", connector.protocol, connector.address, connector.port));
            connecterUrlList.add(connectorUrlInfo);
        }
        return connecterUrlList;
    }
}
