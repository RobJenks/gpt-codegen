package org.rj.modelgen.bpmn.models.generation.validation;

public class PayloadVariable {
    String name;
    String type;

    public PayloadVariable() {
    }

    public PayloadVariable(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PayloadVariable that = (PayloadVariable) obj;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result += (type != null ? type.hashCode() : 0);
        return result;
    }
}
