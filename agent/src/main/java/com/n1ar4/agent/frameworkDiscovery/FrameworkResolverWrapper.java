package com.n1ar4.agent.frameworkDiscovery;

import com.n1ar4.agent.frameworkDiscovery.Spring.httpReqeuestHandler.HttpRequestHandlerResolver;
import com.n1ar4.agent.frameworkDiscovery.Spring.springframework.SpringInfo;

public enum FrameworkResolverWrapper {

    Spring(new SpringInfo("org.springframework.web.servlet.DispatcherServlet")),
    SpringRequestHandlerServlet(new HttpRequestHandlerResolver("org.springframework.web.context.support.HttpRequestHandlerServlet")),
    ;
    private FrameworkResolver frameworkResolver;

    FrameworkResolverWrapper(FrameworkResolver frameworkResolver) {
        this.frameworkResolver = frameworkResolver;
    }

    public FrameworkResolver getFrameworkResolver() {
        return frameworkResolver;
    }
}
