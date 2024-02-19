package org.rj.modelgen.bpmn.generation;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rj.modelgen.llm.schema.IntermediateModelParser;
import org.rj.modelgen.llm.util.Util;

import java.util.stream.IntStream;

public class TestBulkBpmnGeneration {

    @Test
    public void testBulkBpmnGeneration() throws Exception {
        final var inputCount = 6;
        final var parser = new IntermediateModelParser();
        final var input = IntStream.rangeClosed(1, inputCount)
                .mapToObj(n -> String.format("generation-examples/input/example-%d-input.json", n))
                .map(Util::loadStringResource)
                .map(parser::parse)
                .map(res -> res.getValueIfPresent().orElseThrow(() -> new RuntimeException("Failed to parse input")))
                .toList();

        final var generator = new BulkBpmnModelGenerator();
        final var results = generator.convert(input, false);

        int i = 1;
        for (final var res : results) {
            System.out.println("Output " + i + ":");
            System.out.println(Bpmn.convertToString(res));
            i += 1;
        }

        Assertions.assertEquals(inputCount, results.size());
    }

}
