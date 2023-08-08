package org.rj.codegen.codegenservice.gpt.beans;

import org.rj.codegen.codegenservice.util.Constants;

public class ContextEntry {
    private String role;
    private String content;

    public static ContextEntry forUser(String content) {
        return new ContextEntry(Constants.ROLE_USER, content);
    }

    public static ContextEntry forAssistant(String content) {
        return new ContextEntry(Constants.ROLE_ASSISTANT, content);
    }

    public ContextEntry() { }

    public ContextEntry(String role, String content) {
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
