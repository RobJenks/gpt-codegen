package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.rj.modelgen.llm.intrep.graph.IntermediateGraphModel;

/**
 * Implementation of graph-specialized IR for BPMN models
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BpmnIntermediateModel extends IntermediateGraphModel<String, ElementConnection, ElementNode> { }
