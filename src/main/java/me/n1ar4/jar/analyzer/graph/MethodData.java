package me.n1ar4.jar.analyzer.graph;

import java.util.List;

public class MethodData {
    private String uuid;
    private String methodString;
    private List<String> callerIds;
    private List<String> calleeIds;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMethodString() {
        return methodString;
    }

    public void setMethodString(String methodString) {
        this.methodString = methodString;
    }

    public List<String> getCallerIds() {
        return callerIds;
    }

    public void setCallerIds(List<String> callerIds) {
        this.callerIds = callerIds;
    }

    public List<String> getCalleeIds() {
        return calleeIds;
    }

    public void setCalleeIds(List<String> calleeIds) {
        this.calleeIds = calleeIds;
    }
}
