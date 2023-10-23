package me.n1ar4.jar.analyzer.entity;

public class InterfaceEntity {
    private int iid;
    private String interfaceName;
    private String className;

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "InterfaceEntity{" +
                "iid=" + iid +
                ", interfaceName='" + interfaceName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
