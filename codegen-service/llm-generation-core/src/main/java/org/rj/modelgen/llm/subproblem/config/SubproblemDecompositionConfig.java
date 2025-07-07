package org.rj.modelgen.llm.subproblem.config;

import org.rj.modelgen.llm.subproblem.states.CombineSubproblems;
import org.rj.modelgen.llm.subproblem.states.GenerateSubproblems;
import org.rj.modelgen.llm.subproblem.states.impl.CombineSubproblemsNaive;
import org.rj.modelgen.llm.subproblem.states.impl.GenerateSubproblemsNaive;

import java.util.function.Supplier;

public class SubproblemDecompositionConfig {
    private Supplier<? extends GenerateSubproblems> subproblemGeneratorImplementation;
    private Supplier<? extends CombineSubproblems> subproblemCombinationImplementation;

    public SubproblemDecompositionConfig() {
        this.subproblemGeneratorImplementation = GenerateSubproblemsNaive::new;
        this.subproblemCombinationImplementation = CombineSubproblemsNaive::new;
    }

    // Return a set of default config - subproblem decomposition is disabled by default
    public static SubproblemDecompositionConfig defaultConfig() {
        return new SubproblemDecompositionConfig();
    }

    public Supplier<? extends GenerateSubproblems> getSubproblemGeneratorImplementation() {
        return subproblemGeneratorImplementation;
    }

    public void setSubproblemGeneratorImplementation(Supplier<? extends GenerateSubproblems> subproblemGeneratorImplementation) {
        this.subproblemGeneratorImplementation = subproblemGeneratorImplementation;
    }

    public SubproblemDecompositionConfig withSubproblemGeneratorImplementation(Supplier<? extends GenerateSubproblems> subproblemGeneratorImplementation) {
        setSubproblemGeneratorImplementation(subproblemGeneratorImplementation);
        return this;
    }

    public Supplier<? extends CombineSubproblems> getSubproblemCombinationImplementation() {
        return subproblemCombinationImplementation;
    }

    public void setSubproblemCombinationImplementation(Supplier<? extends CombineSubproblems> subproblemCombinationImplementation) {
        this.subproblemCombinationImplementation = subproblemCombinationImplementation;
    }

    public SubproblemDecompositionConfig withSubproblemCombinationImplementation(Supplier<? extends CombineSubproblems> subproblemCombinationImplementation) {
        setSubproblemCombinationImplementation(subproblemCombinationImplementation);
        return this;
    }
}
