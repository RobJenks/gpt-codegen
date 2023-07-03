package org.rj.codegen.codegenservice.context;

import io.micrometer.common.util.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.rj.codegen.codegenservice.bpmn.beans.ConnectionNode;
import org.rj.codegen.codegenservice.bpmn.beans.ElementNode;
import org.rj.codegen.codegenservice.bpmn.beans.NodeData;
import org.rj.codegen.codegenservice.bpmn.generation.BasicBpmnModelGenerator;
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

    final BasicBpmnModelGenerator generator = new BasicBpmnModelGenerator();

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

        final var startNode = new ElementNode("startProcess", "Start Process", "startEvent");
        startNode.setConnectedTo(List.of(new ElementNode.Connection("endProcess", "End the process immediately")));
        nodeData.getNodes().add(startNode);
        nodeData.getNodes().add(new ElementNode("endProcess", "End Process", "endEvent"));

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

    private String previousConstrainedPrompt(String prompt) {
        return String.format("""
                You are building a business process model following the BPMN 2.0 standard. It consists of nodes which can be connected to show the flow of execution. Observe the following constraints:
                * You can use any node types supported in the BPMN 2.0 specification.  Try to select a specific task type appropriate for the step, e.g. a userTask for actions requested from users, or serviceTask for a call to a system
                * You should use exclusive gateways to handle branches based on some condition
                * Nodes can have a single connection to another node unless they are 'gateway' node types
                
                Extend the model you just returned as follows: %s
                
                Return the result as a JSON object containing
                * A list "elements" of elements with fields (id, name, type, properties), where 'id' is a unique single-word camel-case identifier, 'name' is a descriptive name, 'type' is one of the permitted node types, and 'properties' is a string map of any properties required to configure the node
                * A list "connections" of connections between nodes, with fields (element1, element2, comment) where element1 connects to element2, and 'comment' is a brief explanation of the connection

                Return only this JSON result and no other text.  Your model must comply with the BPMN 2.0 specification.
                """, prompt);
    }

    private String constrainedPrompt2(String prompt) {
        return String.format("""
                You are building a business process model following the BPMN 2.0 standard. It consists of nodes which can be connected to denote the flow of execution. Observe the following constraints:
                * You can use any node types supported in the BPMN 2.0 specification.  Try to select a specific task type appropriate for the step, e.g. a userTask for actions requested from users, or serviceTask for a call to a system
                * You should use 'exclusive gateways' to handle branches based on a condition
                * Nodes can have a single connection to another node unless they are 'gateway' node types
                
                Your model is represented as a JSON object containing a list "elements".  Each item of the list has the following properties:
                * 'type': A valid BPMN 2.0 element type, 
                * 'id': A unique single-word camel-case identifier
                * 'name': A descriptive name
                
                
                Return the result as a JSON object containing
                * A list "elements" of elements with fields (id, name, type, properties), where 'id' is a unique single-word camel-case identifier, 'name' is a descriptive name, 'type' is one of the permitted node types, and 'properties' is a string map of any properties required to configure the node
                * A list "connections" of connections between nodes, with fields (element1, element2, comment) where element1 connects to element2, and 'comment' is a brief explanation of the connection

                Extend the model you just returned as follows: %s
                
                Return only this JSON result and no other text.  Your model must comply with the BPMN 2.0 specification.
                """, prompt);
    }

    private String constrainedPrompt(String prompt) {
        return String.format("""
                You are designing a business process model following the BPMN 2.0 standard.  You should take the existing JSON process definition (complying to the JSON schema below) from your last response, and a description of updates to make to that process from this prompt.  You must update the existing process design by the following process:
                1. Parse the list of existing nodes and connections based on the JSON schema, and interpret it as a series of steps including decision points and branches
                2. Modify this series of steps as requested by the prompt
                3. Convert your new series of steps to a list of nodes and connections.  Return this data as JSON complying to the JSON Schema below
                4. Verify that your returned data complies to the schema and all requirements.  If it does not, regenerate it until it is correct.
                                
                The JSON schema you should use for parsing and generating the JSON data is as follows:
                ```
                {
                  "$id": "https://example.com/process-model.json",
                  "$schema": "https://json-schema.org/draft/2020-12/schema",
                  "title": "Process model",
                  "type": "object",
                  "additionalProperties": false,
                  "properties": {
                    "nodes": {
                      "type": "array",
                      "items": {
                        "$ref": "#/$defs/element"
                      }
                    }
                  },
                  "$defs": {
                    "element": {
                      "type": "object",
                      "additionalProperties": false,
                      "properties": {
                        "id": {
                          "$ref": "#/$defs/elementId",
                          "description": "A unique identifier for this node.  Camelcase string based on the name."
                        },
                        "name": {
                          "type": "string",
                          "description": "A descriptive name for this node"
                        },
                        "elementType": {
                          "description": "An element type valid in the BPMN 2.0 standard",
                          "oneOf": [
                            { "const": "startEvent", "description": "The start point of a process.  Only one is allowed" },
                            { "const": "endEvent", "description": "An end point of a process.  May be more than one" },
                            { "const": "userTask", "description": "A task issued to a user by the automation system" },
                            { "const": "serviceTask", "description": "A programmatic call to an system by the process" },
                            { "const": "scriptTask", "description": "Execution of some internal script logic within the process, for example to manipulate data from the previous step" },
                            { "const": "businessRuleTask", "description": "A call to evaluate a DMN business decision table" },
                            { "const": "manualTask", "description": "A task performed manually by a user" },
                            { "const": "sendTask", "description": "" },
                            { "const": "receiveTask", "description": "" },
                            { "const": "callTask", "description": "" },
                            { "const": "exclusiveGateway", "description": "A decision point which may branch in several direction based on the data received from the previous node.  Allows multiple output connections" },
                            { "const": "inclusiveGateway", "description": "Acts as the target for multiple branches which converge at this point.  One output connection" }
                          ]
                        },
                        "connectedTo": {
                          "type": [
                            "string",
                            "array"
                          ]
                        }
                      },
                      "allOf": [
                        {
                          "if": {
                            "properties": {
                              "elementType": {
                                "const": "exclusiveGateway"
                              }
                            }
                          },
                          "then": {
                            "properties": {
                              "connectedTo": {
                                "type": "array",
                                "items": {
                                  "$ref": "#/$defs/nodeConnection",
                                  "description": "The IDs of all nodes which this exclusive gateway node connects to.  Outbound connections only.  This must comply with the BPMN 2.0 standard.  Most other element types cannot have multiple outgoing connections"
                                }
                              }
                            }
                          },
                          "else": {
                            "properties": {
                              "connectedTo": {
                                "type": "array",
                                "maxItems": 1,
                                "items": {
                                  "$ref": "#/$defs/nodeConnection",
                                  "description": "The single node which this node connects to, if any.  Outbound connection only.  May be missing if no connections"
                                }
                              }
                            }
                          }
                        }
                      ],
                      "required": [
                        "id",
                        "name",
                        "elementType"
                      ]
                    },
                    "elementId": {
                      "type": "string",
                      "description": "A unique identifier for a node"
                    },
                    "nodeConnection": {
                      "type": "object",
                      "properties": {
                        "targetNode": {
                          "$ref": "#/$defs/elementId",
                          "description": "The target node of this connection"
                        },
                        "description": {
                          "type": "string",
                          "description": "A short text description of this connection"
                        }
                      },
                      "required": [
                        "targetNode"
                      ]
                    }
                  }
                }
                ```
                Other requirements:
                * Most element types cannot have multiple outgoing connections.  If you wish to branch in multiple directions, you should use an exclusiveGateway node type to branch based upon the result of the previous node
                * Your process should begin with a Start Event and end with one or more End Events
                * You cannot use a generic "task" type.  Instead, choose the most appropriate BPMN 2.0 element type.  For example, a userTask for actions requested of a user, or a serviceTask for systematic calls to another system
                * You can modify any parts of the existing process definition
                                
                The updates that you should make to the process definition before returning the new JSON data are as follows:\s
                ```
                %s
                ```
                Return ONLY the JSON process data, with no other explanation or commentary.
                """, prompt);
    }

    @Override
    public List<String> validateResponse(String response) {
        if (StringUtils.isBlank(response)) return List.of("Empty prompt");

        // Basic sanitizing; attempt to locate the largest JSON block within the output, in case of additional text
        // in violation of prompt constraints
        final var sanitized = sanitizeResponse(response);

        NodeData nodeData = null;
        try {
            // Make sure the response can be deserialized into the requested structure
            nodeData = Util.getObjectMapper().readValue(sanitized, NodeData.class);
        }
        catch (Exception ex) {
            return List.of("Response does not conform to required schema");
        }

        // Validate the node data complies with all our requirements
//        final var nodeDataErrors = generator.validateNodeData(nodeData);
//        if (!nodeDataErrors.isEmpty()) return nodeDataErrors;
        // TODO: Use JSON Schema validation instead

        // Passed all checks
        return List.of();
    }

    @Override
    public String getValidationFailureRetryPrompt(String responseFailingValidation, List<String> validationErrors) {
        return String.format("Your response does not meet the requirements: %s. Correct your response and return only the JSON " +
                "data with no other commentary or explanation", String.join(";", validationErrors));
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

    @Override
    public String generateTransformedOutput(String response) {
        final var nodeData = Util.deserializeOrThrow(response, NodeData.class,
                e -> new RuntimeException("Failed to deserialize last response into required data: " + e.getMessage(), e));

        final var model = generator.generateModel(nodeData);
        final var serialized = Bpmn.convertToString(model);

        return serialized;
    }
}
