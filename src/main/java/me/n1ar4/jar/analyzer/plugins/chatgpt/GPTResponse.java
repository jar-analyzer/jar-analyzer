package me.n1ar4.jar.analyzer.plugins.chatgpt;

public class GPTResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private GPTChoice[] choices;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public GPTChoice[] getChoices() {
        return choices;
    }

    public void setChoices(GPTChoice[] choices) {
        this.choices = choices;
    }
}
