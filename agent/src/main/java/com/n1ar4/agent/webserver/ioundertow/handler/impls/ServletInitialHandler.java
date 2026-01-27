package com.n1ar4.agent.webserver.ioundertow.handler.impls;

import com.n1ar4.agent.webserver.FrameworkBaseInfo;
import com.n1ar4.agent.webserver.ioundertow.handler.BasicHandlerResolver;
import com.n1ar4.agent.webserver.ioundertow.UndertowFrameworkInfo;
import com.n1ar4.agent.webserver.ioundertow.urlInfo.UnderTowConnectorInfo;
import com.n1ar4.agent.webserver.ioundertow.urlInfo.UnderTowEndpointUrlInfo;
import com.n1ar4.agent.util.ReflectUtils;
import com.n1ar4.agent.framework.FrameworkResolver;
import com.n1ar4.agent.framework.FrameworkResolverWrapper;
import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.SourceResultType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServletInitialHandler extends BasicHandlerResolver {

    private String ContextPath;
    private UnderTowConnectorInfo connectorInfo;
    private boolean hasDefaultServlet = false;
    private Map<String, UnderTowEndpointUrlInfo> servletUrlMap = new HashMap<String, UnderTowEndpointUrlInfo>();
    private Map<String, UnderTowEndpointUrlInfo> filterUrlMap = new HashMap<String, UnderTowEndpointUrlInfo>();


    private void ParseOneManagedServlet(Object managedServlet, String servletName) {
        Object servletInfo = ReflectUtils.getDeclaredField(managedServlet, "servletInfo");
        if (servletInfo == null) {
            return;
        }
        List<String> mappings = (List<String>) ReflectUtils.getDeclaredField(servletInfo, "mappings");
        if (mappings == null) {
            return;
        }
        UnderTowEndpointUrlInfo underTowEndpointUrlInfo = new UnderTowEndpointUrlInfo(connectorInfo, this.ContextPath);
        if (mappings.size() > 0) {
            for (String mapping : mappings) {
                if (mapping.equals("/")) {
                    hasDefaultServlet = true;
                    underTowEndpointUrlInfo.patterns.add("/*");
                } else {
                    underTowEndpointUrlInfo.patterns.add(mapping);
                }
            }
        } else { // for default servlet
            underTowEndpointUrlInfo.patterns.add("/");
        }
        this.servletUrlMap.put(servletName, underTowEndpointUrlInfo);
        ArrayList<String> desc = new ArrayList<String>();
        Map<String, String> initParams = (Map<String, String>) ReflectUtils.getDeclaredField(servletInfo, "initParams");
        if (initParams != null && initParams.size() > 0) {
            desc.add("initParams : ");
            for (Map.Entry<String, String> initParam : initParams.entrySet()) {
                desc.add(String.format("\t%s => %s", initParam.getKey(), initParam.getValue()));
            }
        }

        String servletClass = ((Class) ReflectUtils.getDeclaredField(servletInfo, "servletClass")).getName();
        this.sourceResults.add(new SourceResult(SourceResultType.ioUnderTowServlet, servletName, servletClass, underTowEndpointUrlInfo.toUrlInfos(), desc));


        Object instanceStrategy = ReflectUtils.getDeclaredField(managedServlet, "instanceStrategy");
        if (instanceStrategy == null) {
            return;
        }

        Object instanceHandle = ReflectUtils.getDeclaredField(instanceStrategy, "handle");
        if (instanceHandle == null) {
            ReflectUtils.callMethod(instanceStrategy, "start");
            instanceHandle = ReflectUtils.getDeclaredField(instanceStrategy, "handle");
            if (instanceHandle == null) {
                return;
            }
        }
        Object instance = ReflectUtils.getDeclaredField(instanceHandle, "instance");
        if (instance == null) {
            return;
        }
        this.AddFramework(instance, underTowEndpointUrlInfo);
    }

    private void GetServlets(Object deployment) {
        Object servlets = ReflectUtils.getDeclaredField(deployment, "servlets");
        if (servlets == null) {
            return;
        }
        Map<String, Object> servletsMap = (Map<String, Object>) ReflectUtils.getDeclaredField(servlets, "managedServletMap");
        if (servletsMap == null) {
            return;
        }
        Object defaultManagedServlet = null;
        for (Map.Entry<String, Object> entry : servletsMap.entrySet()) {
            String servletName = entry.getKey();
            Object servletHandler = entry.getValue();
            if (servletHandler == null) {
                continue;
            }
            Object managedServlet = ReflectUtils.getDeclaredField(servletHandler, "managedServlet");
            if (managedServlet == null) {
                continue;
            }
            if (servletName.equals("default")) {
                defaultManagedServlet = managedServlet;
                continue;
            }
            ParseOneManagedServlet(managedServlet, servletName);
        }
        if (hasDefaultServlet == false) {
            ParseOneManagedServlet(defaultManagedServlet, "default");
        }
    }

    private void parseFilterUrlMaps(Object deploymentInfo) {
        if (this.filterUrlMap.size() > 0) {
            return;
        }
        List<Object> filterUrlMappings = (List<Object>) ReflectUtils.getDeclaredField(deploymentInfo, "filterUrlMappings");
        if (filterUrlMappings == null) {
            return;
        }
        List<Object> filterNameUrlMappings = (List<Object>) ReflectUtils.getDeclaredField(deploymentInfo, "filterServletNameMappings");
        if (filterNameUrlMappings == null) {
            return;
        }

        UnderTowEndpointUrlInfo underTowEndpointUrlInfo = new UnderTowEndpointUrlInfo(connectorInfo, this.ContextPath);
        for (Object filterUrlMapping : filterUrlMappings) {
            String filterName = (String) ReflectUtils.getDeclaredField(filterUrlMapping, "filterName");
            String mapping = (String) ReflectUtils.getDeclaredField(filterUrlMapping, "mapping");
            Enum dispatcher = (Enum) ReflectUtils.getDeclaredField(filterUrlMapping, "dispatcher");
            if (this.filterUrlMap.containsKey(filterName) == false) {
                this.filterUrlMap.put(filterName, new UnderTowEndpointUrlInfo(connectorInfo, this.ContextPath));
            }
            this.filterUrlMap.get(filterName).patterns.add(mapping);
            this.filterUrlMap.get(filterName).desc.add(String.format("mapping %s : dispatchType : %s", mapping, dispatcher.name()));
        }

        for (Object filterNameUrlMapping : filterNameUrlMappings) {
            String filterName = (String) ReflectUtils.getDeclaredField(filterNameUrlMapping, "filterName");
            String servletName = (String) ReflectUtils.getDeclaredField(filterNameUrlMapping, "mapping");
            Enum dispatcher = (Enum) ReflectUtils.getDeclaredField(filterNameUrlMapping, "dispatcher");
            if (this.filterUrlMap.containsKey(filterName) == false) {
                this.filterUrlMap.put(filterName, new UnderTowEndpointUrlInfo(connectorInfo, this.ContextPath));
            }
            if (this.servletUrlMap.containsKey(servletName)) {
                this.filterUrlMap.get(filterName).patterns.addAll(this.servletUrlMap.get(servletName).patterns);
                this.filterUrlMap.get(filterName).desc.add(String.format("servletName %s : dispatchType : %s", servletName, dispatcher.name()));
            }
        }
    }

    private void GetFilters(Object deployment) {
        Object deploymentInfo = ReflectUtils.getDeclaredField(deployment, "deploymentInfo");
        if (deploymentInfo == null) {
            return;
        }
        parseFilterUrlMaps(deploymentInfo);

        Object filters = ReflectUtils.getDeclaredField(deployment, "filters");
        if (filters == null) {
            return;
        }
        Map<String, Object> filtersMap = (Map<String, Object>) ReflectUtils.getDeclaredField(filters, "managedFilterMap");
        if (filtersMap == null) {
            return;
        }


        for (Map.Entry<String, Object> stringObjectEntry : filtersMap.entrySet()) {
            String filterName = stringObjectEntry.getKey();
            Object managedFilter = stringObjectEntry.getValue();
            if (managedFilter == null) {
                continue;
            }
            Object filterInfo = ReflectUtils.getDeclaredField(managedFilter, "filterInfo");
            if (filterInfo == null) {
                continue;
            }
            String filterClassName = (String) ((Class) ReflectUtils.getDeclaredField(filterInfo, "filterClass")).getName();

            UnderTowEndpointUrlInfo nowUndertowEndpointUrlInfo = null;
            if (this.filterUrlMap.containsKey(filterName)) {
                nowUndertowEndpointUrlInfo = this.filterUrlMap.get(filterName);
            } else {
                nowUndertowEndpointUrlInfo = new UnderTowEndpointUrlInfo(connectorInfo, this.ContextPath);
            }

            ArrayList<String> desc = new ArrayList<String>();
            Map<String, String> initParams = (Map<String, String>) ReflectUtils.getDeclaredField(filterInfo, "initParams");
            if (initParams != null && initParams.size() > 0) {
                desc.add("initParams : ");
                for (Map.Entry<String, String> initParam : initParams.entrySet()) {
                    desc.add(String.format("\t%s => %s", initParam.getKey(), initParam.getValue()));
                }
            }

            if (nowUndertowEndpointUrlInfo.desc.size() > 0) {
                desc.addAll(nowUndertowEndpointUrlInfo.desc);
            }
            this.sourceResults.add(new SourceResult(SourceResultType.ioUnderTowFilter, filterName, filterClassName, nowUndertowEndpointUrlInfo.toUrlInfos(), desc));
            Object handle = ReflectUtils.getDeclaredField(managedFilter, "handle");
            if (handle == null) {
                continue;
            }

            Object instance = ReflectUtils.getDeclaredField(handle, "instance");
            if (instance == null) {
                continue;
            }
            this.AddFramework(instance, nowUndertowEndpointUrlInfo);
        }
    }

    @Override
    protected void resolverInternal(Object handler, UnderTowConnectorInfo connectorInfo) {
        this.connectorInfo = connectorInfo;
        if (handler == null) {
            return;
        }

        Object paths = ReflectUtils.getDeclaredField(handler, "paths");
        if (paths == null) {
            return;
        }
        Object deployment = ReflectUtils.getDeclaredField(paths, "deployment");
        if (deployment == null) {
            return;
        }
        Object deploymentInfo = ReflectUtils.getDeclaredField(deployment, "deploymentInfo");
        if (deploymentInfo == null) {
            return;
        }
        Object contextPath = ReflectUtils.getDeclaredField(deploymentInfo, "contextPath");
        if (contextPath == null) {
            return;
        }
        this.ContextPath = (String) contextPath;
        GetServlets(deployment);
        GetFilters(deployment);
    }


    public void AddFramework(Object instance, UnderTowEndpointUrlInfo patternInfo) {
        if (instance != null) {
            String className = instance.getClass().getName();
            for (FrameworkResolverWrapper frameworkResolverWrapper : FrameworkResolverWrapper.values()) {
                FrameworkResolver frameworkResolver = frameworkResolverWrapper.getFrameworkResolver();
                if (frameworkResolver.canResolve(className)) {
                    String resolverName = frameworkResolverWrapper.name();
                    if (!this.frameworkInstances.containsKey(resolverName))
                        this.frameworkInstances.put(resolverName, new ArrayList<FrameworkBaseInfo>());
                    this.frameworkInstances.get(resolverName).add(new UndertowFrameworkInfo(instance, patternInfo));
                    break;
                }
            }
        }
    }

    @Override
    public List<Object> getNextHandlers() {
        return super.getNextHandlers();
    }
}
