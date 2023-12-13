package org.rj.modelgen.llm.integrations.openai;

public class OpenAIContextMessage {
    private String role;
    private String content;

    public OpenAIContextMessage() {
    }

    public OpenAIContextMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

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
}
