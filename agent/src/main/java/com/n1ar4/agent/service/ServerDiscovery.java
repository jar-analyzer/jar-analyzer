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

package com.n1ar4.agent.service;

import arthas.VmTool;
import arthas.core.util.SearchUtils;
import com.n1ar4.agent.dto.SourceResult;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;

public abstract class ServerDiscovery {
    protected String serverClass;

    public ServerDiscovery(String serverClass) {
        this.serverClass = serverClass;
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
