package com.n1ar4.agent.frameworkDiscovery.Spring.httpReqeuestHandler;

import com.n1ar4.agent.Utils.ReflectUtils;
import com.n1ar4.agent.frameworkDiscovery.FrameworkResolver;
import com.n1ar4.agent.dto.SourceResult;
import com.n1ar4.agent.dto.SourceResultType;
import com.n1ar4.agent.dto.UrlInfo;

import java.util.ArrayList;

public class HttpRequestHandlerResolver extends FrameworkResolver {
    public HttpRequestHandlerResolver(String resolverClass) {
        super(resolverClass);
    }

    @Override
    protected ArrayList<SourceResult> resolveInternal(Object instance, ArrayList<UrlInfo> baseUrlInfos) {
        Object target = ReflectUtils.getDeclaredField(instance, "target");
        if (target == null) return null;

        ArrayList<SourceResult> sourceResults = new ArrayList<SourceResult>();
        sourceResults.add(new SourceResult(SourceResultType.SpringBeanNameController, target.getClass().getSimpleName(), target.getClass().getName(), baseUrlInfos));
        return sourceResults;
    }

    @Override
    public boolean isKeep() {
        return false;
    }
}
