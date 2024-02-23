package me.n1ar4.rasp.agent.ent;

import java.util.List;

public class UploadMessage {
    private int vulType;
    private String vulTypeName;
    private List<String> stacks;
    private Object input;

    public int getVulType() {
        return vulType;
    }

    public void setVulType(int vulType) {
        this.vulType = vulType;
    }

    public String getVulTypeName() {
        return vulTypeName;
    }

    public void setVulTypeName(String vulTypeName) {
        this.vulTypeName = vulTypeName;
    }

    public List<String> getStacks() {
        return stacks;
    }

    public void setStacks(List<String> stacks) {
        this.stacks = stacks;
    }

    public Object getInput() {
        return input;
    }

    public void setInput(Object input) {
        this.input = input;
    }
}
