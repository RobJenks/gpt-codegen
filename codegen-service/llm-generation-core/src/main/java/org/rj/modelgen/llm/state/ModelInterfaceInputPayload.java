package org.rj.modelgen.llm.state;

import org.rj.modelgen.llm.statemodel.data.common.StandardModelData;

/**
 * Specialization of standard model payload with a set of explicit mandatory & optional fields
 */
public class ModelInterfaceInputPayload extends ModelInterfacePayload {

    public ModelInterfaceInputPayload(String sessionId, String request) {
        // Mandatory values
        put(StandardModelData.SessionId, sessionId);
        put(StandardModelData.Request, request);

        // Default values for optional parameters
        put(StandardModelData.Llm, "gpt-4");
        put(StandardModelData.Temperature, 0.7);
    }

    /* Standard fields required for the input payload */

    public String getSessionId() {
        return get(StandardModelData.SessionId);
    }

    public void setSessionId(String sessionId) {
        put(StandardModelData.SessionId, sessionId);
    }

    public String getRequest() {
        return get(StandardModelData.Request);
    }

    public void setRequest(String request) {
        put(StandardModelData.Request, request);
    }

    public String getLlm() {
        return get(StandardModelData.Llm);
    }

    public void setLlm(String llm) {
        put(StandardModelData.Llm, llm);
    }

    public double getTemperature() {
        return get(StandardModelData.Temperature);
    }

    public void setTemperature(double temperature) {
        put(StandardModelData.Temperature, temperature);
    }
}
