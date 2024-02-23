package me.n1ar4.rasp.agent.ent;

public class HookInfo implements Vul {
    private String className;
    private String methodName;
    private String methodDesc;
    private int vulTYpe;

    public int getVulTYpe() {
        return vulTYpe;
    }

    public void setVulTYpe(int vulTYpe) {
        this.vulTYpe = vulTYpe;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }
}
