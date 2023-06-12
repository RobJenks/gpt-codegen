package org.rj.codegen.codegenservice.context;

import io.micrometer.common.util.StringUtils;
import org.rj.codegen.codegenservice.bpmn.beans.ConnectionNode;
import org.rj.codegen.codegenservice.bpmn.beans.ElementNode;
import org.rj.codegen.codegenservice.bpmn.beans.NodeData;
import org.rj.codegen.codegenservice.gpt.beans.ContextEntry;
import org.rj.codegen.codegenservice.gpt.beans.PromptContextSubmission;
import org.rj.codegen.codegenservice.gpt.beans.SessionState;
import org.rj.codegen.codegenservice.util.Constants;
import org.rj.codegen.codegenservice.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class BpmnGeneratingContextProvider extends ContextProvider {
    private static final Logger LOG = LoggerFactory.getLogger(BpmnGeneratingContextProvider.class);
    private static final String REJECT_TOKEN = "NO";
    private static final Pattern JSON_EXTRACT = Pattern.compile("^.*?(\\{.*}).*?$", Pattern.DOTALL | Pattern.MULTILINE);


    public BpmnGeneratingContextProvider() {
    }

    @Override
    public PromptContextSubmission buildBody(SessionState session, String prompt) {
        return buildBodyWithDecoration(session, prompt, this::constrainedPrompt);
    }

    @Override
    public PromptContextSubmission buildUndecoratedBody(SessionState session, String prompt) {
        return buildBodyWithDecoration(session, prompt, Function.identity());
    }

    public PromptContextSubmission buildBodyWithDecoration(SessionState session, String prompt, Function<String, String> decorator) {
        final var context = session.hasLastResponse() ?
                withContinuationContext(session, decorator.apply(prompt)) :
                withInitialContext(decorator.apply(prompt));

        final var body = PromptContextSubmission.defaultConfig(context);
        LOG.info("Request body: {}", Util.serializeOrThrow(body));

        return body;
    }



    private List<ContextEntry> withInitialContext(String prompt) {
        return List.of(
                ContextEntry.forAssistant(buildInitialNodeData().serialize()),
                ContextEntry.forUser(prompt)
        );
    }

    private NodeData buildInitialNodeData() {
        final var nodeData = new NodeData();

        nodeData.getElements().add(new ElementNode("start", "Start", "startEvent"));
        nodeData.getElements().add(new ElementNode("end", "End", "endEvent"));

        nodeData.getConnections().add(new ConnectionNode("start", "end", ""));

        return nodeData;
    }

    private List<ContextEntry> withContinuationContext(SessionState session, String prompt) {
        String lastValidCode = null;
        for (int i = session.getEvents().size() - 1; i >= 0; --i) {
            final var event = session.getEvents().get(i);

            if (!event.getRole().equals(Constants.ROLE_ASSISTANT)) continue;
            if (event.getContent().equals(REJECT_TOKEN)) continue;

            lastValidCode = event.getContent();
            break;
        }

        if (lastValidCode == null) {
            return withInitialContext(prompt);
        }

        return List.of(
                ContextEntry.forAssistant(lastValidCode),
                ContextEntry.forUser(prompt)
        );
    }

    private String previousConstrainedPrompt(String prompt) {
        return String.format("""
                Extend the BPMN model as follows: %s

                Return the result as a JSON object containing
                * A list "elements" of BPMN nodes with fields (name, type, x, y), where 'type' is a standard BPMN 2.0 node type, and 'x'/'y' are layout coordinates
                * A list "connections" of connections between nodes, with fields (element1, element2, comment) where element1 connects to element2, and 'comment' is a brief explanation of the connection

                Return only this JSON result and no other text""", prompt);
    }

    private String prev2ConstrainedPrompt(String prompt) {
        return String.format("""
                You are building a business process model. It consists of nodes which can be connected to show the flow of execution. Node can have a single connection to another 
                node unless they are 'gateway' node types.  You can use the following node types:
                * Start event - a single node which represents the start of the process
                * End event - represents the termination of the process
                * User task - to collect information from or display information to a user
                * Service task - to call a system or service
                * Script tasks - to run some custom script logic
                * Gateways - represent a branch.  There can be multiple connections from a gateway to other nodes
                
                Extend the current model as follows: %s
                
                Return the result as a JSON object containing
                * A list "elements" of elements with fields (name, type, x, y), where 'type' is one of the permitted node types, and 'x'/'y' are layout coordinates
                * A list "connections" of connections between nodes, with fields (element1, element2, comment) where element1 connects to element2, and 'comment' is a brief explanation of the connection

                Return only this JSON result and no other text
                """, prompt);
    }

    private String constrainedPrompt(String prompt) {
        return String.format("""
                You are building a business process model following the BPMN 2.0 standard. It consists of nodes which can be connected to show the flow of execution. Observe the following constraints:
                * You can use any node types supported in the BPMN 2.0 specification.  Try to select a specific task type appropriate for the step, e.g. a userTask for actions requested from users, or serviceTask for a call to a system
                * You should use exclusive gateways to handle branches based on some condition
                * Nodes can have a single connection to another node unless they are 'gateway' node types
                
                Extend the model you just returned as follows: %s
                
                Return the result as a JSON object containing
                * A list "elements" of elements with fields (id, name, type, properties), where 'id' is a unique single-word camel-case identifier, 'name' is a descriptive name, 'type' is one of the permitted node types, and 'properties' is a string map of any properties required to configure the node
                * A list "connections" of connections between nodes, with fields (element1, element2, comment) where element1 connects to element2, and 'comment' is a brief explanation of the connection

                Return only this JSON result and no other text
                """, prompt);
    }

    @Override
    public List<String> validateResponse(String response) {
        if (StringUtils.isBlank(response)) return List.of("Empty prompt");

        try {
            // Basic sanitizing; attempt to locate the largest JSON block within the output, in case of additional text
            // in violation of prompt constraints
            final var sanitized = sanitizeResponse(response);

            // The response is valid if it can be deserialized into the requested structure
            Util.getObjectMapper().readValue(sanitized, NodeData.class);
            return List.of();
        }
        catch (Exception ex) {
            return List.of("Response is not valid JSON (%s): %s", ex.getMessage(), response);
        }
    }

    @Override
    public String getValidationFailureRetryPrompt(String responseFailingValidation) {
        return "Your response does not meet the requirements.  Ensure it does and is valid JSON.  Return the full JSON and ONLY the JSON";
    }

    @Override
    public String sanitizeResponse(String response) {
        if (StringUtils.isBlank(response)) return response;

        // In case the model has ignored the "only the JSON" constraint, attempt to locate the largest JSON block within the response
        final var matcher = JSON_EXTRACT.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return response;
    }
}
