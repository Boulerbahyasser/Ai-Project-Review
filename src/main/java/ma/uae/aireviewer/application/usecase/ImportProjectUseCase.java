package ma.uae.aireviewer.application.usecase;

import ma.uae.aireviewer.application.dto.ImportProjectRequest;
import ma.uae.aireviewer.project.loader.ProjectLoaderFactory;
import ma.uae.aireviewer.project.model.SoftwareProject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Cas d'utilisation : importer un projet et construire son arborescence. */
public final class ImportProjectUseCase {
    private static final Map<String, SoftwareProject> PROJECTS = new ConcurrentHashMap<>();

    private final ProjectLoaderFactory loaderFactory;

    public ImportProjectUseCase(ProjectLoaderFactory loaderFactory) {
        this.loaderFactory = loaderFactory;
    }

    public SoftwareProject execute(ImportProjectRequest request) {
        if (request == null || request.source() == null) {
            throw new IllegalArgumentException("Source d'import obligatoire");
        }
        SoftwareProject project = loaderFactory.loaderFor(request.source()).load(request.source());
        PROJECTS.put(project.metadata().location().toString(), project);
        PROJECTS.put(project.metadata().name(), project);
        return project;
    }

    public static SoftwareProject find(String id) {
        return id == null ? null : PROJECTS.get(id);
    }
}
