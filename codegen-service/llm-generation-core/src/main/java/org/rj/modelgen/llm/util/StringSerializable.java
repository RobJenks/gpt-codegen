package org.rj.modelgen.llm.util;

public interface StringSerializable {
    String toString();


    static RawString Raw() {
        return Raw(null);
    }

    static RawString Raw(String content) {
        return new RawString(content);
    }

    class RawString implements StringSerializable {
        private final String content;

        public RawString() {
            this(null);
        }

        public RawString(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
