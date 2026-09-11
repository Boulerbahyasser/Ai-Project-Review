package ma.uae.aireviewer.project.loader;

import ma.uae.aireviewer.project.classification.FileClassifier;
import ma.uae.aireviewer.project.model.DirectoryNode;
import ma.uae.aireviewer.project.model.FileNode;
import ma.uae.aireviewer.project.model.ProjectOrigin;
import ma.uae.aireviewer.project.model.ProjectMetadata;
import ma.uae.aireviewer.project.model.SoftwareProject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;

/** Import depuis un repertoire local. */
public final class DirectoryProjectLoader implements ProjectLoader {

    private final FileClassifier classifier;

    public DirectoryProjectLoader(FileClassifier classifier) {
        this.classifier = java.util.Objects.requireNonNull(classifier, "classifier");
    }

    @Override
    public boolean supports(ProjectSource source) {
        return source instanceof ProjectSource.LocalDirectory;
    }

    @Override
    public SoftwareProject load(ProjectSource source) {
        if (!(source instanceof ProjectSource.LocalDirectory local)) {
            throw new ProjectImportException("Source non supportee par ce loader");
        }
        Path root = local.directory();
        if (root == null || !Files.isDirectory(root)) {
            throw new ProjectImportException("Repertoire introuvable : " + root);
        }
        try {
            Path normalized = root.toRealPath();
            DirectoryNode tree = readDirectory(normalized);
            String name = normalized.getFileName() == null ? normalized.toString()
                    : normalized.getFileName().toString();
            return new SoftwareProject(
                    new ProjectMetadata(name, normalized, ProjectOrigin.DIRECTORY, Instant.now()), tree);
        } catch (IOException failure) {
            throw new ProjectImportException("Lecture impossible de " + root, failure);
        }
    }

    private DirectoryNode readDirectory(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            java.util.List<ma.uae.aireviewer.project.model.ProjectNode> children = paths
                .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                .map(path -> {
                    try {
                        if (Files.isSymbolicLink(path)) {
                            return null;
                        }
                        if (Files.isDirectory(path)) {
                            return readDirectory(path);
                        }
                        if (Files.isRegularFile(path)) {
                            return new FileNode(path.getFileName().toString(), path,
                                    Files.size(path), classifier.classify(path));
                        }
                        return null;
                    } catch (IOException failure) {
                        throw new ProjectImportException("Lecture impossible de " + path, failure);
                    }
                })
                .filter(java.util.Objects::nonNull)
                .map(node -> (ma.uae.aireviewer.project.model.ProjectNode) node)
                .toList();
            return new DirectoryNode(directory.getFileName() == null ? directory.toString()
                    : directory.getFileName().toString(), directory, children);
        }
    }
}
