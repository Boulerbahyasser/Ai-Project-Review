package ma.uae.aireviewer.project.loader;

import java.util.List;

/** Pattern Factory : selectionne le loader capable de traiter la source. */
public final class ProjectLoaderFactory {

    private final List<ProjectLoader> loaders;

    public ProjectLoaderFactory(List<ProjectLoader> loaders) {
        this.loaders = List.copyOf(loaders);
    }

    public ProjectLoader loaderFor(ProjectSource source) {
        return loaders.stream()
                .filter(loader -> loader.supports(source))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aucun loader pour : " + source));
    }
}
