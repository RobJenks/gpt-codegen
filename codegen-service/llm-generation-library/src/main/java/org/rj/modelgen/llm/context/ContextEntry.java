package org.rj.modelgen.llm.context;

import org.rj.modelgen.llm.util.Constants;

public class ContextEntry {
    private ContextRole role;
    private String content;

    public static ContextEntry forUser(String content) {
        return new ContextEntry(ContextRole.USER, content);
    }

    public static ContextEntry forModel(String content) {
        return new ContextEntry(ContextRole.MODEL, content);
    }

    public ContextEntry() { }

    public ContextEntry(ContextRole role, String content) {
        this.role = role;
        this.content = content;
    }

    public ContextRole getRole() {
        return role;
    }

    public void setRole(ContextRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
