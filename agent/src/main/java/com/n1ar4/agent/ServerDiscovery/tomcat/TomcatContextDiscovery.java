package com.n1ar4.agent.ServerDiscovery.tomcat;

import com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo.ContextInfo;
import com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo.EndPointUrlInfo;
import com.n1ar4.agent.ServerDiscovery.tomcat.SourceInfo.HostInfo;
import com.n1ar4.agent.sourceResult.SourceResult;
import com.n1ar4.agent.sourceResult.SourceResultType;
import com.n1ar4.agent.util.ReflectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class TomcatContextDiscovery {
    private final Object context;
    ArrayList<SourceResult> sourceList;
    private ContextInfo contextUrlInfo;
    // for Filter Get Url
    HashMap<String, EndPointUrlInfo> servletUrlInfoMap;

    public TomcatContextDiscovery(Object contextInstance, HostInfo hostUrlInfo) {
        this.context = contextInstance;

        this.contextUrlInfo = new ContextInfo(hostUrlInfo);
        try {
            String ContextUrlBase = (String) ReflectUtils.callMethod(context, "getPath");
            this.contextUrlInfo.ContextUrlBase = ContextUrlBase;
        } catch (Exception e) {
//            this.contextUrlBase = hostUrlInfo;
        }

        this.servletUrlInfoMap = new HashMap<String, EndPointUrlInfo>();
        this.sourceList = new ArrayList<SourceResult>();
    }

    void getAllListener() {
        CopyOnWriteArrayList listeners = (CopyOnWriteArrayList) ReflectUtils.getDeclaredField(context, "applicationEventListenersList");
        if (listeners != null) {
            for (Object listener : listeners.toArray()) {
                Class<?> listenerClass = listener.getClass();
                EndPointUrlInfo endPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                endPointUrlInfo.urlPatterns.add("");
                sourceList.add(new SourceResult(SourceResultType.TomcatListener, listenerClass.getSimpleName(), listenerClass.getName(), endPointUrlInfo.toUrlInfos()));
            }
            return;
        }

        Object[] applicationEventListeners = (Object[]) ReflectUtils.callMethod(context, "getApplicationEventListeners");
        if (applicationEventListeners != null) {
            for (Object listener : applicationEventListeners) {
                Class<?> listenerClass = listener.getClass();
                EndPointUrlInfo endPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                endPointUrlInfo.urlPatterns.add("");
                sourceList.add(new SourceResult(SourceResultType.TomcatListener, listenerClass.getSimpleName(), listenerClass.getName(), endPointUrlInfo.toUrlInfos()));
            }
            return;
        }


        Object[] applicationListeners = (Object[]) ReflectUtils.getDeclaredField(context, "applicationListeners");
        if (applicationListeners != null) {
            for (Object applicationListener : applicationListeners) {
                if (applicationListener.getClass().getName().equals("".getClass().getName())) {
                    EndPointUrlInfo endPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                    endPointUrlInfo.urlPatterns.add("");
                    sourceList.add(new SourceResult(SourceResultType.TomcatListener, (String) applicationListener, (String) applicationListener, endPointUrlInfo.toUrlInfos()));
                } else {
                    String listenerClassName = (String) ReflectUtils.getDeclaredField(applicationListener, "className");
                    EndPointUrlInfo endPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                    endPointUrlInfo.urlPatterns.add("");

                    sourceList.add(new SourceResult(SourceResultType.TomcatListener, listenerClassName, listenerClassName, endPointUrlInfo.toUrlInfos()));
                }

            }
        }
    }

    void getFilters() {
        Object FilterMaps = ReflectUtils.getDeclaredField(context, "filterMaps");
        if (FilterMaps == null) return;

        Object[] filterMapArray = (Object[]) ReflectUtils.getDeclaredField(FilterMaps, "array");

        if (filterMapArray != null && filterMapArray.length > 0) {
            HashMap<String, Object> filterDefs = (HashMap<String, Object>) ReflectUtils.getDeclaredField(context, "filterDefs");
            if (filterDefs == null) return;

            for (Object filterMap : filterMapArray) {
                String filterName = (String) ReflectUtils.getDeclaredField(filterMap, "filterName");
                String[] urlPatterns = (String[]) ReflectUtils.getDeclaredField(filterMap, "urlPatterns");
                String[] ServletNames = (String[]) ReflectUtils.getDeclaredField(filterMap, "servletNames");

                Object FilterDef = filterDefs.get(filterName);
                String filterClass = (String) ReflectUtils.getDeclaredField(FilterDef, "filterClass");

                ArrayList<String> filterDesc = new ArrayList<String>();
                HashMap<String, String> paramters = (HashMap<String, String>) ReflectUtils.getDeclaredField(FilterDef, "parameters");
                if (paramters != null && paramters.size() > 0) {
                    filterDesc.add("parameters : ");
                    for (Map.Entry<String, String> parameter : paramters.entrySet()) {
                        String parameterKey = parameter.getKey();
                        String paramterValue = parameter.getValue();
                        filterDesc.add(String.format("\t %s => %s", parameterKey, paramterValue));
                    }
                }


                EndPointUrlInfo nowFilterEndPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                for (String urlPattern : urlPatterns) {
                    nowFilterEndPointUrlInfo.urlPatterns.add(urlPattern);
                }

                for (String servletName : ServletNames) {
                    nowFilterEndPointUrlInfo.urlPatterns.addAll(servletUrlInfoMap.get(servletName).urlPatterns);
                }

                Object filterConfig = ReflectUtils.callMethod(context, "findFilterConfig", filterName);
                Object filter = ReflectUtils.getDeclaredField(filterConfig, "filter");

                sourceList.add(new SourceResult(SourceResultType.TomcatFilter, filterName, filterClass, nowFilterEndPointUrlInfo.toUrlInfos(), filterDesc));
            }
        }
    }

    void getAllServlets() {
        HashMap<String, ArrayList<String>> filterDescs = new HashMap<String, ArrayList<String>>();
        Object[] wrappers = (Object[]) ReflectUtils.callMethod(context, "findChildren");
        if (wrappers == null) return;

        int servletNumber = wrappers.length;
//        OutputUtils.getPrintStream().println("Servlet Numbers : " + servletNumber);

        if (servletNumber > 0) {
            HashMap<String, String> servletMappings = (HashMap<String, String>) ReflectUtils.getDeclaredField(context, "servletMappings");
            if (servletMappings == null) return;

            for (Map.Entry<String, String> entry : servletMappings.entrySet()) {
                String servletName = entry.getValue();
                String servletMapping = entry.getKey();

                if (this.servletUrlInfoMap.containsKey(servletName) == false) {
                    servletUrlInfoMap.put(servletName, new EndPointUrlInfo(contextUrlInfo));
                }
                servletUrlInfoMap.getOrDefault(servletName, new EndPointUrlInfo(contextUrlInfo)).urlPatterns.add(servletMapping);
            }

            for (Object wrapper : wrappers) {
                String servletName = (String) ReflectUtils.callMethod(wrapper, "getServletName");
                String servletClass = (String) ReflectUtils.callMethod(wrapper, "getServletClass");
                EndPointUrlInfo nowTomcatEndPointUrlInfo = servletUrlInfoMap.get(servletName);
                if (nowTomcatEndPointUrlInfo == null) {
                    continue;
                }

                if (isNeedGetWebService(servletName)) {
                    getAllWebService();
                } else {
                    sourceList.add(new SourceResult(SourceResultType.TomcatServlet, servletName, servletClass, nowTomcatEndPointUrlInfo.toUrlInfos(), filterDescs.get(servletName)));
                }
            }
        }
    }

    boolean isNeedGetWebService(String servletName) {
        return servletName.equals("Dynamic JAXWS Servlet");
    }

    void getAllWebService() {
        Object applicationContext = ReflectUtils.getDeclaredField(context, "context");
        Object wsServletDelegate = ReflectUtils.callMethod(applicationContext, "getAttribute", "com.sun.xml.ws.server.http.servletDelegate");

        ArrayList<Object> servletAdapters = (ArrayList<Object>) ReflectUtils.getDeclaredField(wsServletDelegate, "adapters");

        if (servletAdapters != null) {
            int adapterSize = servletAdapters.size();
            if (adapterSize > 0) {
                for (Object servletAdapter : servletAdapters) {
                    String adapterName = (String) ReflectUtils.callMethod(servletAdapter, "getName");
                    String adapterUrlPattern = (String) ReflectUtils.getDeclaredField(servletAdapter, "urlPattern");
                    Object endPoint = ReflectUtils.callMethod(servletAdapter, "getEndpoint");
                    Class<?> implementationClass = (Class<?>) ReflectUtils.getDeclaredField(endPoint, "implementationClass");

                    EndPointUrlInfo webserviceEndPointUrlInfo = new EndPointUrlInfo(contextUrlInfo);
                    webserviceEndPointUrlInfo.urlPatterns.add(adapterUrlPattern);
                    sourceList.add(new SourceResult(SourceResultType.TomcatWebService, adapterName, implementationClass.getName(), webserviceEndPointUrlInfo.toUrlInfos()));
                }
            }
        }
    }


    public ArrayList<SourceResult> getSourceResults() {
        getAllListener();
        getFilters();
        getAllServlets();

        return sourceList;
    }
}
