package ma.uae.aireviewer.project.loader;

import ma.uae.aireviewer.project.classification.FileClassifier;
import ma.uae.aireviewer.project.model.SoftwareProject;

/**
 * Import depuis une archive (zip/tar). Doit se proteger du zip-slip :
 * toute entree resolue hors du repertoire d'extraction est rejetee.
 */
public final class ArchiveProjectLoader implements ProjectLoader {

    private final FileClassifier classifier;

    public ArchiveProjectLoader(FileClassifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public boolean supports(ProjectSource source) {
        return source instanceof ProjectSource.Archive;
    }

    @Override
    public SoftwareProject load(ProjectSource source) {
        throw new UnsupportedOperationException("TODO : extraire l'archive (protection zip-slip) puis deleguer");
    }
}
