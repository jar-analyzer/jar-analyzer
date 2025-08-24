/*
 * GPLv3 License
 *
 * Copyright (c) 2023-2025 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.taint;

import me.n1ar4.jar.analyzer.core.reference.MethodReference;

import java.util.Objects;

public class CallGraph {
    private final MethodReference.Handle callerMethod;
    private final MethodReference.Handle targetMethod;
    private final int callerArgIndex;
    private final int targetArgIndex;

    public CallGraph(MethodReference.Handle callerMethod, MethodReference.Handle targetMethod,
                     int callerArgIndex, int targetArgIndex) {
        this.callerMethod = callerMethod;
        this.targetMethod = targetMethod;
        this.callerArgIndex = callerArgIndex;
        this.targetArgIndex = targetArgIndex;
    }

    public MethodReference.Handle getCallerMethod() {
        return callerMethod;
    }

    public MethodReference.Handle getTargetMethod() {
        return targetMethod;
    }

    public int getCallerArgIndex() {
        return callerArgIndex;
    }

    public int getTargetArgIndex() {
        return targetArgIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CallGraph CallGraph = (CallGraph) o;

        if (callerArgIndex != CallGraph.callerArgIndex) {
            return false;
        }
        if (targetArgIndex != CallGraph.targetArgIndex) {
            return false;
        }
        if (!Objects.equals(callerMethod, CallGraph.callerMethod)) {
            return false;
        }
        return Objects.equals(targetMethod, CallGraph.targetMethod);
    }

    @Override
    public int hashCode() {
        int result = callerMethod != null ? callerMethod.hashCode() : 0;
        result = 31 * result + (targetMethod != null ? targetMethod.hashCode() : 0);
        result = 31 * result + callerArgIndex;
        result = 31 * result + targetArgIndex;
        return result;
    }
}
