package org.rj.modelgen.llm.state;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rj.modelgen.llm.statemodel.signals.common.StandardSignals;
import org.rj.modelgen.llm.statemodel.states.common.ExecuteLogic;
import org.rj.modelgen.llm.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public class ModelInterfaceStateMachineTest {
    private static final Logger LOG = LoggerFactory.getLogger(ModelInterfaceStateMachineTest.class);

    @Test
    public void testBasicModelConfiguration() throws Exception {
        final var model = createBasicModel();

        final var layout = model.getLayoutInfo();

        Assertions.assertEquals(Set.of(
                new ModelInterfaceTransitionRule.Reference("A", "Success", "B"),
                new ModelInterfaceTransitionRule.Reference("B", "Success", "C")
        ), layout);
    }

    @Test
    public void testInsertStatesInExistingModel() throws Exception {
        final var model = createBasicModel()
                .withModelCustomization(modelData -> {
                    final var newStateX = testState("X");
                    final var newStateY = testState("Y");

                    return new ModelInterfaceStateMachineCustomization()
                            .withNewStateInsertedAfter(newStateX, "A")
                            .withNewStateInsertedAfter(newStateY, "B");
                });

        final var layout = model.getLayoutInfo();

        Assertions.assertEquals(Set.of(
                new ModelInterfaceTransitionRule.Reference("A", "Success", "X"),
                new ModelInterfaceTransitionRule.Reference("X", "Success", "B"),
                new ModelInterfaceTransitionRule.Reference("B", "Success", "Y"),
                new ModelInterfaceTransitionRule.Reference("Y", "Success", "C")
        ), layout);
    }

    private ModelInterfaceStateMachine createBasicModel() {
        final var states = List.of(testState("A"), testState("B"), testState("C"));
        final var rules = new ModelInterfaceTransitionRules(List.of(
                testRule(states.get(0), states.get(1)),
                testRule(states.get(1), states.get(2))
        ));

        return new ModelInterfaceStateMachine(ModelInterfaceStateMachine.class, null, states, rules);
    }

    private ModelInterfaceState testState(String id) {
        return new ExecuteLogic() {
            @Override
            protected Mono<Result<Void, String>> executeLogic() {
                LOG.info("Executing node {}", getId());
                return Mono.just(Result.Ok());
            }
        }
        .withOverriddenId(id);
    }

    private ModelInterfaceTransitionRule testRule(ModelInterfaceState from, ModelInterfaceState to) {
        return testRule(from, to, StandardSignals.SUCCESS);
    }

    private ModelInterfaceTransitionRule testRule(ModelInterfaceState from, ModelInterfaceState to, String signalType) {
        return new ModelInterfaceTransitionRule(from, signalType, to);
    }

}
