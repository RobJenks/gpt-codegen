package org.rj.modelgen.llm.beans;

public class AuditEntry {
    private String name;
    private String content;

    public AuditEntry() {
    }

    public AuditEntry(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
