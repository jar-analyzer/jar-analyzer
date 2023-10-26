package me.n1ar4.jar.analyzer.chatgpt;

public class GPTRequest {
    private String model;
    private Message[] messages;
    private double temperature;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(Message[] messages) {
        this.messages = messages;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public GPTRequest(String input) {
        this.model = "gpt-3.5-turbo";
        this.temperature = 0.7;
        this.messages = new Message[]{new Message(input)};
    }
}
