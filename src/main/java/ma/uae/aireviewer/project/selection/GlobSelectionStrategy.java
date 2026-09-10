package ma.uae.aireviewer.project.selection;

import java.util.List;
import ma.uae.aireviewer.project.model.FileNode;

/** Selection par motifs glob d'inclusion et d'exclusion. */
public final class GlobSelectionStrategy implements FileSelectionStrategy {

    private final List<String> includeGlobs;
    private final List<String> excludeGlobs;

    public GlobSelectionStrategy(List<String> includeGlobs, List<String> excludeGlobs) {
        this.includeGlobs = List.copyOf(includeGlobs);
        this.excludeGlobs = List.copyOf(excludeGlobs);
    }

    @Override
    public String name() {
        return "glob";
    }

    @Override
    public boolean accepts(FileNode file) {
        throw new UnsupportedOperationException("TODO : evaluer les PathMatcher d'inclusion puis d'exclusion");
    }
}
