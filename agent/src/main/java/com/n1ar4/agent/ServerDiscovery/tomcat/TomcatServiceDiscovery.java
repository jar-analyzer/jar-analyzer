package com.n1ar4.agent.ServerDiscovery.tomcat;

import com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo.HostInfo;
import com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo.ServiceInfo;
import com.n1ar4.agent.sourceResult.SourceResult;
import com.n1ar4.agent.sourceResult.UrlInfo;
import com.n1ar4.agent.util.ReflectUtils;

import java.util.ArrayList;

public class TomcatServiceDiscovery {
    private ServiceInfo serviceUrlInfo;
    private Object engine;

    public TomcatServiceDiscovery(Object service) {
        this.serviceUrlInfo = new ServiceInfo();
        Object[] connectors = (Object[]) ReflectUtils.getDeclaredField(service, "connectors");

        if (connectors != null) {
            for (Object connector : connectors) {
                String protocol = (String) ReflectUtils.callMethod(connector, "getProtocol");
                if(protocol.contains("AJP"))
                    System.out.println("Contains AJP");
                if (protocol != null && !protocol.equals("AJP/1.3")) {
                    Object oname = ReflectUtils.callMethod(connector, "getObjectName");
                    String onameString = oname.toString();
                    String[] properties = onameString.split(",");
                    String host = "0.0.0.0";
                    String port = "";
                    for (String property : properties) {
                        if (property.startsWith("port=")) {
                            port = property.split("port=")[1];
                        }
                        if (property.startsWith("address=")) {
                            host = property.split("address=")[1];
                            if (host.startsWith("\""))
                                host = host.substring(1);
                            host = host.replaceAll("\"|\'", "");
                        }
                    }
                    String connectorInfo = null;
                    String schemaPrefix;
                    if (port.contains("443") || port.contains("8443"))
                        schemaPrefix = "https://";
                    else
                        schemaPrefix = "http://";
                    if (port.equals("")) {
                        connectorInfo = String.format("%s%s", schemaPrefix, host);
                    } else {
                        connectorInfo = String.format("%s%s:%s", schemaPrefix, host, port);
                    }

                    String connectorDesc = String.format("protocol: %s" , protocol);
                    this.serviceUrlInfo.connectorList.add(new UrlInfo(connectorInfo, connectorDesc));
                }
            }
        }

        this.engine = ReflectUtils.getDeclaredField(service, "engine");
        if (this.engine == null) this.engine = ReflectUtils.getDeclaredField(service, "container");
    }

    public ArrayList<SourceResult> getSourceResults() {
        ArrayList<SourceResult> sourceResults = new ArrayList<SourceResult>();

        if (engine != null) {
            String defaultHost = (String) ReflectUtils.getDeclaredField(engine, "defaultHost");
            this.serviceUrlInfo.defaultHost = defaultHost;
            Object[] hosts = (Object[]) ReflectUtils.callMethod(engine, "findChildren");

            if (hosts != null && hosts.length > 0) {
                for (Object host : hosts) {
                    String hostName = (String) ReflectUtils.callMethod(host, "getName");
                    Object[] contexts = (Object[]) ReflectUtils.callMethod(host, "findChildren");
                    HostInfo tomcatHostUrlInfo = new HostInfo(serviceUrlInfo, hostName);
                    if (contexts != null && contexts.length > 0) {
                        for (Object context : contexts) {
                            TomcatContextDiscovery tomcatContextSourceDiscovery = new TomcatContextDiscovery(context, tomcatHostUrlInfo);
                            sourceResults.addAll(tomcatContextSourceDiscovery.getSourceResults());
                        }
                    }
                }
            }
        }
        return sourceResults;
    }
}
