package ma.uae.aireviewer.application.usecase;

import ma.uae.aireviewer.application.dto.ImportProjectRequest;
import ma.uae.aireviewer.project.loader.ProjectLoaderFactory;
import ma.uae.aireviewer.project.model.SoftwareProject;

/** Cas d'utilisation : importer un projet et construire son arborescence. */
public final class ImportProjectUseCase {

    private final ProjectLoaderFactory loaderFactory;

    public ImportProjectUseCase(ProjectLoaderFactory loaderFactory) {
        this.loaderFactory = loaderFactory;
    }

    public SoftwareProject execute(ImportProjectRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
