package org.rj.modelgen.llm.component;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ComponentLibrary<TComponent extends Component> {
    private List<TComponent> components;

    public ComponentLibrary() {
        this(new ArrayList<>());
    }
    public ComponentLibrary(List<TComponent> components) {
        this.components = Optional.ofNullable(components).orElseGet(ArrayList::new);
    }

    public List<TComponent> getComponents() {
        return components;
    }

    public void setComponents(List<TComponent> components) {
        this.components = components;
    }

    @JsonIgnore
    public ComponentLibrary<TComponent> getFilteredLibrary(Predicate<TComponent> predicate) {
        return new ComponentLibrary<>(
                getComponents().stream()
                        .filter(predicate)
                        .collect(Collectors.toList()));
    }

    /**
     * Serialize a component library into its default representation.  Should be overridden for different
     * library types and usage scenarios (high level, detail level, ...)
     *
     * @return String serialized component library
     */
    public String defaultSerialize() {
        return Optional.ofNullable(components).orElseGet(List::of).stream()
                .map(Component::defaultSerialize)
                .collect(Collectors.joining("\n"));
    }
}
