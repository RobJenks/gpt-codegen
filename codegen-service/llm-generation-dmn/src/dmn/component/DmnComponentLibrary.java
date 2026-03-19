package dmn.component;

import org.rj.modelgen.llm.component.ComponentLibrary;

import java.util.ArrayList;
import java.util.List;

public class DmnComponentLibrary extends ComponentLibrary<DmnComponent> {
    private List<DmnComponent> components;

    public DmnComponentLibrary() {
        super();
    }

    public DmnComponentLibrary(DmnComponentLibrary template) {
        this(new ArrayList<>(template.getComponents()));
    }

    public DmnComponentLibrary(List<DmnComponent> components) {
        super(components);
    }

    @Override
    public ComponentLibrary<DmnComponent> constructEmpty() {
        return new DmnComponentLibrary();
    }

    @Override
    public List<DmnComponent> getComponents() {
        return components;
    }

    @Override
    public void setComponents(List<DmnComponent> components) {
        this.components = components;
    }
}
