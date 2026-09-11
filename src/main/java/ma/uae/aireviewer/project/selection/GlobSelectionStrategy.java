package ma.uae.aireviewer.project.selection;

import java.util.List;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Path;
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
        String value = file.path().toString().replace('\\', '/');
        String name = file.name();
        boolean included = includeGlobs.isEmpty() || includeGlobs.stream()
                .map(this::matcher)
                .anyMatch(matcher -> matcher.matches(file.path())
                        || matcher.matches(Path.of(value))
                        || matcher.matches(Path.of(name)));
        boolean excluded = excludeGlobs.stream()
                .map(this::matcher)
                .anyMatch(matcher -> matcher.matches(file.path())
                        || matcher.matches(Path.of(value))
                        || matcher.matches(Path.of(name)));
        return included && !excluded;
    }

    private PathMatcher matcher(String glob) {
        String normalized = glob.replace('\\', '/');
        return FileSystems.getDefault().getPathMatcher(
                normalized.startsWith("glob:") ? normalized : "glob:" + normalized);
    }
}
