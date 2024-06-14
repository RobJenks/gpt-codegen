package org.rj.modelgen.bpmn.generation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rj.modelgen.bpmn.intrep.model.BpmnIntermediateModel;
import org.rj.modelgen.llm.intrep.IntermediateModelParser;
import org.rj.modelgen.llm.util.Util;

import java.util.stream.IntStream;

public class TestBulkBpmnGeneration {

    @Test
    public void testBulkBpmnGeneration() throws Exception {
        final var inputCount = 6;
        final var parser = new IntermediateModelParser<>(BpmnIntermediateModel.class);
        final var input = IntStream.rangeClosed(1, inputCount)
                .mapToObj(n -> String.format("generation-examples/base/input/example-%d-input.json", n))
                .map(Util::loadStringResource)
                .map(parser::parse)
                .map(res -> res.getValueIfPresent().orElseThrow(() -> new RuntimeException("Failed to parse input")))
                .toList();

        final var generator = new BulkBpmnModelGenerator();
        final var results = generator.convert(input, false);

        // Assert all models were generated correctly
        Assertions.assertEquals(inputCount, results.size());
    }
}
