package ma.uae.aireviewer.project.loader;

import ma.uae.aireviewer.project.classification.FileClassifier;
import ma.uae.aireviewer.project.model.SoftwareProject;

/** Import depuis un repertoire local. */
public final class DirectoryProjectLoader implements ProjectLoader {

    private final FileClassifier classifier;

    public DirectoryProjectLoader(FileClassifier classifier) {
        this.classifier = classifier;
    }

    @Override
    public boolean supports(ProjectSource source) {
        return source instanceof ProjectSource.LocalDirectory;
    }

    @Override
    public SoftwareProject load(ProjectSource source) {
        throw new UnsupportedOperationException("TODO : parcourir le repertoire et construire le Composite");
    }
}
