package org.rj.codegen.codegenservice.bpmn.beans;

public class ConnectionNode {
    private String source;
    private String dest;
    private String comment;

    public ConnectionNode() {
    }

    public ConnectionNode(String source, String dest, String comment) {
        this.source = source;
        this.dest = dest;
        this.comment = comment;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
