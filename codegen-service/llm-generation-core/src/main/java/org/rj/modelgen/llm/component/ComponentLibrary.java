package org.rj.modelgen.llm.component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class ComponentLibrary<TComponent extends Component> {
    public ComponentLibrary() {
        this(new ArrayList<>());
    }
    public ComponentLibrary(List<TComponent> components) {
        setComponents(Optional.ofNullable(components).orElseGet(ArrayList::new));
    }

    public abstract ComponentLibrary<TComponent> constructEmpty();

    public abstract List<TComponent> getComponents();

    public abstract void setComponents(List<TComponent> components);

    @JsonIgnore
    public ComponentLibrary<TComponent> getFilteredLibrary(Predicate<TComponent> predicate) {
        final var filtered = constructEmpty();
        filtered.setComponents(
                getComponents().stream()
                        .filter(predicate)
                        .collect(Collectors.toList()));

        return filtered;
    }

    /**
     * Serialize a component library into its default representation.  Should be overridden for different
     * library types and usage scenarios (high level, detail level, ...)
     *
     * @return String serialized component library
     */
    public String defaultSerialize() {
        return Optional.ofNullable(getComponents()).orElseGet(List::of).stream()
                .map(Component::defaultSerialize)
                .collect(Collectors.joining("\n"));
    }
}
