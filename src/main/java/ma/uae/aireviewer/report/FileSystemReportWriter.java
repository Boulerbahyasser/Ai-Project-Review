package ma.uae.aireviewer.report;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes rendered reports below a configured directory. */
public final class FileSystemReportWriter implements ReportWriter {
    private final Path directory;

    public FileSystemReportWriter(Path directory) {
        this.directory = directory;
    }

    @Override
    public Path write(String renderedContent, String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Nom de rapport invalide");
        }
        try {
            Files.createDirectories(directory);
            Path base = directory.toAbsolutePath().normalize();
            Path output = base.resolve(fileName).normalize();
            if (!output.startsWith(base)) {
                throw new IllegalArgumentException("Chemin de rapport hors repertoire");
            }
            return Files.writeString(output, renderedContent == null ? "" : renderedContent,
                    StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("Ecriture du rapport impossible", failure);
        }
    }
}
