package me.n1ar4.jar.analyzer.sca.dto;

public class CVEData {
    private String cve;
    private float cvss;
    private String desc;

    public String getCve() {
        return cve;
    }

    public void setCve(String cve) {
        this.cve = cve;
    }

    public float getCvss() {
        return cvss;
    }

    public void setCvss(float cvss) {
        this.cvss = cvss;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
