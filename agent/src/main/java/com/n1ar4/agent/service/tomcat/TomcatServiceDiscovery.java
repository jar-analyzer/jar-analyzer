/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.service.tomcat;

import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.UrlInfo;
import com.n1ar4.agent.service.tomcat.info.HostInfo;
import com.n1ar4.agent.service.tomcat.info.ServiceInfo;
import com.n1ar4.agent.Utils.ReflectUtils;

import java.util.ArrayList;

public class TomcatServiceDiscovery {
    private final ServiceInfo serviceUrlInfo;
    private Object engine;

    @SuppressWarnings("all")
    public TomcatServiceDiscovery(Object service) {
        this.serviceUrlInfo = new ServiceInfo();
        Object[] connectors = (Object[]) ReflectUtils.getDeclaredField(service, "connectors");
        if (connectors != null) {
            for (Object connector : connectors) {
                String protocol = (String) ReflectUtils.callMethod(connector, "getProtocol");
                if (protocol.contains("AJP")) {
                    System.out.println("[*] SERVER CONTAINS AJP PROTOCOL");
                }
                if (!protocol.equals("AJP/1.3")) {
                    Object oName = ReflectUtils.callMethod(connector, "getObjectName");
                    String onameString = oName.toString();
                    String[] properties = onameString.split(",");
                    String host = "0.0.0.0";
                    String port = "";
                    for (String property : properties) {
                        if (property.startsWith("port=")) {
                            port = property.split("port=")[1];
                        }
                        if (property.startsWith("address=")) {
                            host = property.split("address=")[1];
                            if (host.startsWith("\"")) {
                                host = host.substring(1);
                            }
                            host = host.replaceAll("[\"']", "");
                        }
                    }
                    String connectorInfo;
                    String schemaPrefix;
                    if (port.contains("443") || port.contains("8443")) {
                        schemaPrefix = "https://";
                    } else {
                        schemaPrefix = "http://";
                    }
                    if (port.isEmpty()) {
                        connectorInfo = String.format("%s%s", schemaPrefix, host);
                    } else {
                        connectorInfo = String.format("%s%s:%s", schemaPrefix, host, port);
                    }
                    String connectorDesc = String.format("protocol: %s", protocol);
                    this.serviceUrlInfo.connectorList.add(new UrlInfo(connectorInfo, connectorDesc));
                }
            }
        }
        this.engine = ReflectUtils.getDeclaredField(service, "engine");
        if (this.engine == null) this.engine = ReflectUtils.getDeclaredField(service, "container");
    }

    public ArrayList<SourceResult> getSourceResults() {
        ArrayList<SourceResult> sourceResults = new ArrayList<>();
        if (engine != null) {
            this.serviceUrlInfo.defaultHost = (String) ReflectUtils.getDeclaredField(engine, "defaultHost");
            Object[] hosts = (Object[]) ReflectUtils.callMethod(engine, "findChildren");
            if (hosts != null) {
                for (Object host : hosts) {
                    String hostName = (String) ReflectUtils.callMethod(host, "getName");
                    Object[] contexts = (Object[]) ReflectUtils.callMethod(host, "findChildren");
                    HostInfo tomcatHostUrlInfo = new HostInfo(serviceUrlInfo, hostName);
                    if (contexts != null) {
                        for (Object context : contexts) {
                            TomcatContextDiscovery tomcatContextSourceDiscovery =
                                    new TomcatContextDiscovery(context, tomcatHostUrlInfo);
                            sourceResults.addAll(tomcatContextSourceDiscovery.getSourceResults());
                        }
                    }
                }
            }
        }
        return sourceResults;
    }
}
