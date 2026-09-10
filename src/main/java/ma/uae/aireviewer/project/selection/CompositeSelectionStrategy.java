package ma.uae.aireviewer.project.selection;

import java.util.List;
import ma.uae.aireviewer.project.model.FileNode;

/** Composition de strategies : un fichier est retenu s'il satisfait toutes les regles. */
public final class CompositeSelectionStrategy implements FileSelectionStrategy {

    private final List<FileSelectionStrategy> strategies;

    public CompositeSelectionStrategy(List<FileSelectionStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    @Override
    public String name() {
        return "all-of";
    }

    @Override
    public boolean accepts(FileNode file) {
        return strategies.stream().allMatch(strategy -> strategy.accepts(file));
    }
}
