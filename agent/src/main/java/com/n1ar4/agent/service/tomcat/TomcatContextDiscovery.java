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

package com.n1ar4.agent.service.tomcat;

import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.SourceResultType;
import com.n1ar4.agent.service.tomcat.info.ContextInfo;
import com.n1ar4.agent.service.tomcat.info.EndPointUrlInfo;
import com.n1ar4.agent.service.tomcat.info.HostInfo;
import com.n1ar4.agent.util.ReflectUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class TomcatContextDiscovery {
    private final Object context;
    ArrayList<SourceResult> sourceList;
    private final ContextInfo contextUrlInfo;
    HashMap<String, EndPointUrlInfo> servletUrlInfoMap;

    public TomcatContextDiscovery(Object contextInstance, HostInfo hostUrlInfo) {
        this.context = contextInstance;
        this.contextUrlInfo = new ContextInfo(hostUrlInfo);
        try {
            this.contextUrlInfo.ContextUrlBase = (String) ReflectUtils.callMethod(context, "getPath");
        } catch (Exception ignored) {
        }
        this.servletUrlInfoMap = new HashMap<>();
        this.sourceList = new ArrayList<>();
    }

    void getAllListener() {
        CopyOnWriteArrayList<?> listeners = (CopyOnWriteArrayList<?>) ReflectUtils.getDeclaredField(
                context, "applicationEventListenersList");
        if (listeners != null) {
            for (Object listener : listeners.toArray()) {
                Class<?> listenerClass = listener.getClass();
                EndPointUrlInfo endPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                endPointUrlInfo.urlPatterns.add("");
                sourceList.add(new SourceResult(SourceResultType.TomcatListener,
                        listenerClass.getSimpleName(), listenerClass.getName(), endPointUrlInfo.toUrlInfos()));
            }
            return;
        }
        Object[] applicationEventListeners = (Object[]) ReflectUtils.callMethod(
                context, "getApplicationEventListeners");
        if (applicationEventListeners != null) {
            for (Object listener : applicationEventListeners) {
                Class<?> listenerClass = listener.getClass();
                EndPointUrlInfo endPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                endPointUrlInfo.urlPatterns.add("");
                sourceList.add(new SourceResult(SourceResultType.TomcatListener,
                        listenerClass.getSimpleName(), listenerClass.getName(), endPointUrlInfo.toUrlInfos()));
            }
            return;
        }
        Object[] applicationListeners = (Object[]) ReflectUtils.getDeclaredField(
                context, "applicationListeners");
        if (applicationListeners != null) {
            for (Object applicationListener : applicationListeners) {
                if (applicationListener.getClass().getName().equals("".getClass().getName())) {
                    EndPointUrlInfo endPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                    endPointUrlInfo.urlPatterns.add("");
                    sourceList.add(new SourceResult(SourceResultType.TomcatListener,
                            (String) applicationListener, (String) applicationListener, endPointUrlInfo.toUrlInfos()));
                } else {
                    String listenerClassName = (String) ReflectUtils.getDeclaredField(
                            applicationListener, "className");
                    EndPointUrlInfo endPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                    endPointUrlInfo.urlPatterns.add("");
                    sourceList.add(new SourceResult(SourceResultType.TomcatListener,
                            listenerClassName, listenerClassName, endPointUrlInfo.toUrlInfos()));
                }

            }
        }
    }

    @SuppressWarnings("all")
    void getFilters() {
        Object FilterMaps = ReflectUtils.getDeclaredField(context, "filterMaps");
        if (FilterMaps == null) {
            return;
        }
        Object[] filterMapArray = (Object[]) ReflectUtils.getDeclaredField(FilterMaps, "array");

        if (filterMapArray != null && filterMapArray.length > 0) {
            HashMap<String, Object> filterDefs = (HashMap<String, Object>)
                    ReflectUtils.getDeclaredField(context, "filterDefs");
            if (filterDefs == null) {
                return;
            }
            for (Object filterMap : filterMapArray) {
                String filterName = (String) ReflectUtils.getDeclaredField(filterMap, "filterName");
                String[] urlPatterns = (String[]) ReflectUtils.getDeclaredField(filterMap, "urlPatterns");
                String[] ServletNames = (String[]) ReflectUtils.getDeclaredField(filterMap, "servletNames");
                Object FilterDef = filterDefs.get(filterName);
                String filterClass = (String) ReflectUtils.getDeclaredField(FilterDef, "filterClass");
                ArrayList<String> filterDesc = new ArrayList<>();
                HashMap<String, String> parameters = (HashMap<String, String>)
                        ReflectUtils.getDeclaredField(FilterDef, "parameters");
                if (parameters != null && !parameters.isEmpty()) {
                    filterDesc.add("parameters : ");
                    for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                        String parameterKey = parameter.getKey();
                        String parameterValue = parameter.getValue();
                        filterDesc.add(String.format("\t %s => %s", parameterKey, parameterValue));
                    }
                }
                EndPointUrlInfo nowFilterEndPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                Collections.addAll(nowFilterEndPointUrlInfo.urlPatterns, Objects.requireNonNull(urlPatterns));
                for (String servletName : Objects.requireNonNull(ServletNames)) {
                    nowFilterEndPointUrlInfo.urlPatterns.addAll(servletUrlInfoMap.get(servletName).urlPatterns);
                }
                sourceList.add(new SourceResult(SourceResultType.TomcatFilter,
                        filterName, filterClass, nowFilterEndPointUrlInfo.toUrlInfos(), filterDesc));
            }
        }
    }

    @SuppressWarnings("all")
    void getAllServlets() {
        Object[] wrappers = (Object[]) ReflectUtils.callMethod(context, "findChildren");
        if (wrappers == null) {
            return;
        }
        int servletNumber = wrappers.length;
        if (servletNumber > 0) {
            HashMap<String, String> servletMappings = (HashMap<String, String>)
                    ReflectUtils.getDeclaredField(context, "servletMappings");
            if (servletMappings == null) {
                return;
            }
            for (Map.Entry<String, String> entry : servletMappings.entrySet()) {
                String servletName = entry.getValue();
                String servletMapping = entry.getKey();
                if (!this.servletUrlInfoMap.containsKey(servletName)) {
                    servletUrlInfoMap.put(servletName, new EndPointUrlInfo(contextUrlInfo));
                }
                servletUrlInfoMap.get(servletName).urlPatterns.add(servletMapping);
            }
            for (Object wrapper : wrappers) {
                String servletName = (String) ReflectUtils.callMethod(wrapper, "getServletName");
                String servletClass = (String) ReflectUtils.callMethod(wrapper, "getServletClass");
                EndPointUrlInfo nowTomcatEndPointUrlInfo = servletUrlInfoMap.get(servletName);
                if (nowTomcatEndPointUrlInfo == null) {
                    continue;
                }
                ArrayList<String> servletDesc = new ArrayList<String>();
                HashMap<String, String> parameters = (HashMap<String, String>)
                        ReflectUtils.getDeclaredField(wrapper, "parameters");
                if (parameters != null && !parameters.isEmpty()) {
                    servletDesc.add("parameters : ");
                    for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                        String parameterKey = parameter.getKey();
                        String parameterValue = parameter.getValue();
                        servletDesc.add(String.format("\t %s => %s", parameterKey, parameterValue));
                    }
                }
                if (isNeedGetWebService(servletName)) {
                    getAllWebService();
                } else {
                    sourceList.add(new SourceResult(SourceResultType.TomcatServlet,
                            servletName, servletClass, nowTomcatEndPointUrlInfo.toUrlInfos(), servletDesc));
                }
            }
        }
    }

    @SuppressWarnings("all")
    boolean isNeedGetWebService(String servletName) {
        return servletName.equals("Dynamic JAXWS Servlet");
    }

    @SuppressWarnings("unchecked")
    void getAllWebService() {
        Object applicationContext = ReflectUtils.getDeclaredField(context, "context");
        Object wsServletDelegate = ReflectUtils.callMethod(applicationContext, "getAttribute",
                "com.sun.xml.ws.server.http.servletDelegate");
        ArrayList<Object> servletAdapters = (ArrayList<Object>)
                ReflectUtils.getDeclaredField(wsServletDelegate, "adapters");
        if (servletAdapters != null) {
            int adapterSize = servletAdapters.size();
            if (adapterSize > 0) {
                for (Object servletAdapter : servletAdapters) {
                    String adapterName = (String) ReflectUtils.callMethod(servletAdapter, "getName");
                    String adapterUrlPattern = (String)
                            ReflectUtils.getDeclaredField(servletAdapter, "urlPattern");
                    Object endPoint = ReflectUtils.callMethod(servletAdapter, "getEndpoint");
                    Class<?> implementationClass = (Class<?>)
                            ReflectUtils.getDeclaredField(endPoint, "implementationClass");
                    EndPointUrlInfo webserviceEndPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                    webserviceEndPointUrlInfo.urlPatterns.add(adapterUrlPattern);
                    sourceList.add(new SourceResult(SourceResultType.TomcatWebService, adapterName,
                            Objects.requireNonNull(implementationClass).getName(),
                            webserviceEndPointUrlInfo.toUrlInfos()));
                }
            }
        }
    }

    public ArrayList<SourceResult> getSourceResults() {
        getAllListener();
        getAllServlets();
        getFilters();
        return sourceList;
    }
}
