package org.rj.modelgen.llm.request;

import java.util.List;
import java.util.Map;

public class ModelRequest {
    private String model;
    private float temperature;
    private List<Message> messages;


    public ModelRequest() { }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }


    public static class Message {
        public enum Role {
            USER,
            MODEL
        }

        private Role role;
        private String message;

        public Message() {
            this(Role.USER, "");
        }

        public Message(Role role, String message) {
            this.role = role;
            this.message = message;
        }

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }

}
