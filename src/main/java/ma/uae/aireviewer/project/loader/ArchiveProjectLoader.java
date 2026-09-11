package ma.uae.aireviewer.project.loader;

import ma.uae.aireviewer.project.classification.FileClassifier;
import ma.uae.aireviewer.project.model.SoftwareProject;
import ma.uae.aireviewer.security.archive.ArchiveExtractor;
import ma.uae.aireviewer.security.archive.ExtractionResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

/**
 * Import depuis une archive. L'extraction est deleguee a ArchiveExtractor
 * (package security.archive) : l'archive n'est jamais decompressee directement
 * sur la machine hote (cahier des charges, section 8).
 */
public final class ArchiveProjectLoader implements ProjectLoader {

    private final FileClassifier classifier;
    private final ArchiveExtractor archiveExtractor;

    public ArchiveProjectLoader(FileClassifier classifier, ArchiveExtractor archiveExtractor) {
        this.classifier = classifier;
        this.archiveExtractor = archiveExtractor;
    }

    @Override
    public boolean supports(ProjectSource source) {
        return source instanceof ProjectSource.Archive;
    }

    @Override
    public SoftwareProject load(ProjectSource source) {
        if (!(source instanceof ProjectSource.Archive archive)) {
            throw new ProjectImportException("Source non supportee par ce loader");
        }
        Path file = archive.archive();
        if (file == null || !Files.isRegularFile(file)) {
            throw new ProjectImportException("Archive introuvable : " + file);
        }
        Path parent = file.toAbsolutePath().getParent();
        Path extraction = parent.resolve(".aireviewer-extract-" + UUID.randomUUID());
        ExtractionResult result = archiveExtractor.extract(file, extraction);
        if (!result.success()) {
            throw new ProjectImportException(
                    "Extraction impossible de " + file + " (" + result.status() + ") : " + result.message());
        }
        DirectoryProjectLoader delegate = new DirectoryProjectLoader(classifier);
        SoftwareProject loaded = delegate.load(new ProjectSource.LocalDirectory(extraction));
        return new SoftwareProject(
                new ma.uae.aireviewer.project.model.ProjectMetadata(
                        file.getFileName().toString(), loaded.metadata().location(),
                        ma.uae.aireviewer.project.model.ProjectOrigin.ARCHIVE, Instant.now()),
                loaded.root());
    }
}
