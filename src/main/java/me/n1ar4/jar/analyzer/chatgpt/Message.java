package me.n1ar4.jar.analyzer.chatgpt;

public class Message {
    private String role;
    private String content;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Message(String input) {
        this.role = "user";
        this.content = input;
    }
}
