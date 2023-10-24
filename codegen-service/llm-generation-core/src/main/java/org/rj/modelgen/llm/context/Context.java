package org.rj.modelgen.llm.context;

import org.jooq.lambda.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Context {
    private final List<ContextEntry> data;

    public Context() {
        this(new ArrayList<>());
    }

    public Context(List<ContextEntry> data) {
        this.data = Optional.ofNullable(data)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
    }

    public Optional<ContextEntry> getLatestUserEntry() {
        return getLatestEntryForRole(ContextRole.USER);
    }

    public Optional<ContextEntry> getLatestModelEntry() {
        return getLatestEntryForRole(ContextRole.MODEL);
    }

    public Optional<ContextEntry> getLatestEntryForRole(ContextRole role) {
        final var length = data.size();
        return IntStream.range(0, length)
                .mapToObj(x -> data.get((length - 1) - x))
                .filter(x -> (x.getRole() == role))
                .findFirst();
    }

    public boolean hasLatestUserEntry() {
        return getLatestUserEntry().isPresent();
    }

    public boolean hasLatestModelEntry() {
        return getLatestModelEntry().isPresent();
    }

    public void addEntry(ContextEntry entry) {
        if (entry == null) return;
        this.data.add(entry);
    }

    public void addUserPrompt(String prompt) {
        addEntry(new ContextEntry(ContextRole.USER, prompt));
    }
    public void addModelResponse(String response) {
        addEntry(new ContextEntry(ContextRole.MODEL, response));
    }

    public Context copy() {
        return new Context(this.data);
    }

    public List<ContextEntry> getData() {
        return data;
    }

    public Stream<ContextEntry> stream() {
        return data.stream();
    }

    public int length() {
        return data.size();
    }
}
