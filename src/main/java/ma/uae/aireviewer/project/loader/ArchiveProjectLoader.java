package ma.uae.aireviewer.project.loader;

import ma.uae.aireviewer.project.classification.FileClassifier;
import ma.uae.aireviewer.project.model.SoftwareProject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
        if (!(source instanceof ProjectSource.Archive archive)) {
            throw new ProjectImportException("Source non supportee par ce loader");
        }
        Path file = archive.archive();
        if (file == null || !Files.isRegularFile(file)) {
            throw new ProjectImportException("Archive introuvable : " + file);
        }
        Path parent = file.toAbsolutePath().getParent();
        Path extraction = parent.resolve(".aireviewer-extract-" + UUID.randomUUID());
        try {
            Files.createDirectories(extraction);
            try (InputStream input = Files.newInputStream(file);
                 ZipInputStream zip = new ZipInputStream(input)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    Path target = extraction.resolve(entry.getName()).normalize();
                    if (!target.startsWith(extraction)) {
                        throw new ProjectImportException("Entree d'archive dangereuse : " + entry.getName());
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            DirectoryProjectLoader delegate = new DirectoryProjectLoader(classifier);
            SoftwareProject loaded = delegate.load(new ProjectSource.LocalDirectory(extraction));
            return new SoftwareProject(
                    new ma.uae.aireviewer.project.model.ProjectMetadata(
                            file.getFileName().toString(), loaded.metadata().location(),
                            ma.uae.aireviewer.project.model.ProjectOrigin.ARCHIVE, Instant.now()),
                    loaded.root());
        } catch (IOException failure) {
            throw new ProjectImportException("Extraction impossible de " + file, failure);
        }
    }
}
