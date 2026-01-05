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
import com.n1ar4.agent.service.ServerDiscovery;
import com.n1ar4.agent.util.ReflectUtils;

import java.util.ArrayList;

public class TomcatServerDiscovery extends ServerDiscovery {
    public TomcatServerDiscovery(String serverClass) {
        super(serverClass);
    }

    @Override
    protected ArrayList<SourceResult> getServerSourceInternal(Object[] instances) {
        Object standardServer = instances[0];
        Object[] services = (Object[]) ReflectUtils.getDeclaredField(standardServer, "services");
        if (services != null) {
            ArrayList<SourceResult> sourceResults = new ArrayList<>();
            for (Object service : services) {
                TomcatServiceDiscovery tomcatServiceSourceDiscovery = new TomcatServiceDiscovery(service);
                sourceResults.addAll(tomcatServiceSourceDiscovery.getSourceResults());
            }
            return sourceResults;
        }
        return null;
    }
}
