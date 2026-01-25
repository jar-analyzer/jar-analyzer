/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package com.n1ar4.agent.service;

import com.agent.vmTool.VmTool;
import com.agent.vmTool.core.util.SearchUtils;
import com.n1ar4.agent.dto.SourceResult;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class BasicServerDiscovery {
    protected String serverClass;
    protected HashMap<String, ArrayList<BasicFrameworkBaseInfo>> frameworkInstances = new HashMap<String, ArrayList<BasicFrameworkBaseInfo>>();

    public BasicServerDiscovery(String serverClass) {
        this.serverClass = serverClass;
    }
    public HashMap<String, ArrayList<BasicFrameworkBaseInfo>> getFrameworkInstances() {
        return frameworkInstances;
    }
    public boolean CanLoad(VmTool vmTool, Instrumentation inst) {
        ArrayList<Class<?>> matchedClasses = new ArrayList<>(SearchUtils.searchClassOnly(
                inst, serverClass, false, null));
        if (matchedClasses.isEmpty())
            return false;
        Object[] instances = vmTool.getInstances(matchedClasses.get(0));
        return instances.length > 0;
    }

    public Object[] getLoadedClasses(VmTool vmTool, Instrumentation inst) {
        ArrayList<Class<?>> matchedClasses = new ArrayList<>(SearchUtils.searchClassOnly(
                inst, serverClass, false, null));
        Class<?> contextClass = matchedClasses.get(0);
        return vmTool.getInstances(contextClass);
    }

    public ArrayList<SourceResult> getServerSources(VmTool vmTool, Instrumentation inst) {
        Object[] instances = getLoadedClasses(vmTool, inst);
        if (instances == null) {
            return new ArrayList<>();
        }
        ArrayList<SourceResult> sourceResults = this.getServerSourceInternal(instances);
        return sourceResults != null ? sourceResults : new ArrayList<>();
    }

    protected abstract ArrayList<SourceResult> getServerSourceInternal(Object[] instances);
}
