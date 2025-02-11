package org.rj.modelgen.llm.intrep.noop;

import org.rj.modelgen.llm.intrep.core.model.IntermediateModel;

import java.util.List;


public class NoOpIntermediateModel implements IntermediateModel {
    private List<NoOpIntermediateModelNode> nodes;

    public NoOpIntermediateModel() { }

    public List<NoOpIntermediateModelNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<NoOpIntermediateModelNode> nodes) {
        this.nodes = nodes;
    }

    public static class NoOpIntermediateModelNode {
        private String id;
        private String ref;
        private String other;

        public NoOpIntermediateModelNode() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRef() {
            return ref;
        }

        public void setRef(String ref) {
            this.ref = ref;
        }

        public String getOther() {
            return other;
        }

        public void setOther(String other) {
            this.other = other;
        }
    }
}
