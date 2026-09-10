package ma.uae.aireviewer.project.loader;

import ma.uae.aireviewer.project.model.SoftwareProject;

/** Import depuis un depot Git (clone superficiel, sans execution de hooks). */
public final class GitProjectLoader implements ProjectLoader {

    @Override
    public boolean supports(ProjectSource source) {
        return source instanceof ProjectSource.GitRepository;
    }

    @Override
    public SoftwareProject load(ProjectSource source) {
        throw new UnsupportedOperationException("TODO : clone --depth 1 puis deleguer a DirectoryProjectLoader");
    }
}
