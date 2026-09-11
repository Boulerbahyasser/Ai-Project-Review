package ma.uae.aireviewer.project.loader;

import ma.uae.aireviewer.project.model.SoftwareProject;
import ma.uae.aireviewer.project.classification.FileClassifier;
import ma.uae.aireviewer.project.classification.RuleBasedFileClassifier;
import ma.uae.aireviewer.project.classification.ClassificationRule;
import ma.uae.aireviewer.project.model.FileType;
import ma.uae.aireviewer.project.model.ProjectMetadata;
import ma.uae.aireviewer.project.model.ProjectOrigin;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Import depuis un depot Git (clone superficiel, sans execution de hooks). */
public final class GitProjectLoader implements ProjectLoader {
    private final FileClassifier classifier;

    public GitProjectLoader() {
        this(defaultClassifier());
    }

    public GitProjectLoader(FileClassifier classifier) {
        this.classifier = java.util.Objects.requireNonNull(classifier, "classifier");
    }

    private static FileClassifier defaultClassifier() {
        return new RuleBasedFileClassifier(List.of(
                rule(path -> path.toString().replace('\\', '/').contains("/test/") ||
                        path.getFileName().toString().toLowerCase().contains("test"), FileType.TEST),
                rule(path -> path.getFileName().toString().endsWith(".java"), FileType.JAVA),
                rule(path -> path.getFileName().toString().equals("pom.xml"), FileType.BUILD_MAVEN),
                rule(path -> path.getFileName().toString().matches("build\\.gradle(\\.kts)?"),
                        FileType.BUILD_GRADLE),
                rule(path -> path.getFileName().toString().equalsIgnoreCase("Dockerfile"), FileType.DOCKER),
                rule(path -> path.getFileName().toString().matches("(?i).+\\.(md|adoc|txt)"),
                        FileType.DOCUMENTATION),
                rule(path -> path.getFileName().toString().matches("(?i).+\\.(ya?ml|properties|json|xml)"),
                        FileType.CONFIGURATION),
                rule(path -> path.getFileName().toString().matches("(?i).+\\.(sh|bat|ps1)"),
                        FileType.SCRIPT)));
    }

    private static ClassificationRule rule(java.util.function.Predicate<Path> predicate, FileType type) {
        return new ClassificationRule() {
            @Override public boolean matches(Path path) { return predicate.test(path); }
            @Override public FileType type() { return type; }
        };
    }

    @Override
    public boolean supports(ProjectSource source) {
        return source instanceof ProjectSource.GitRepository;
    }

    @Override
    public SoftwareProject load(ProjectSource source) {
        if (!(source instanceof ProjectSource.GitRepository repository)
                || repository.url() == null || repository.url().isBlank()) {
            throw new ProjectImportException("URL Git invalide");
        }
        Path destination = Path.of(".aireviewer-git-" + UUID.randomUUID()).toAbsolutePath();
        try {
            Files.createDirectories(destination);
            ProcessBuilder builder = new ProcessBuilder("git", "clone", "--depth", "1");
            if (repository.reference() != null && !repository.reference().isBlank()) {
                builder.command().add("--branch");
                builder.command().add(repository.reference());
            }
            builder.command().add(repository.url());
            builder.command().add(destination.toString());
            Process process = builder.redirectErrorStream(true).start();
            String output = new String(process.getInputStream().readAllBytes());
            if (process.waitFor() != 0) {
                throw new ProjectImportException("Clone Git impossible : " + output);
            }
            SoftwareProject project = new DirectoryProjectLoader(classifier)
                    .load(new ProjectSource.LocalDirectory(destination));
            return new SoftwareProject(
                    new ProjectMetadata(repository.url(), project.metadata().location(),
                            ProjectOrigin.GIT, Instant.now()), project.root());
        } catch (IOException failure) {
            throw new ProjectImportException("Clone Git impossible", failure);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new ProjectImportException("Clone Git interrompu", interrupted);
        }
    }
}
