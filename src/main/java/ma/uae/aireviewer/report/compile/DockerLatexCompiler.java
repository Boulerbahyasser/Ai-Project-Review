package ma.uae.aireviewer.report.compile;

import ma.uae.aireviewer.configuration.LatexConfig;
import ma.uae.aireviewer.security.secret.PatternSecretRedactor;
import ma.uae.aireviewer.security.secret.SecretRedactor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Compilation isolee dans un conteneur, sans shell-escape.
 *
 * <p>Le {@code .tex} est monte seul, en lecture seule ; pdflatex ecrit uniquement
 * dans un repertoire temporaire dedie ({@code -output-directory}), jamais dans le
 * repertoire du rapport. Le PDF produit y est copie apres coup.
 */
public final class DockerLatexCompiler implements PdfCompiler {

    private final LatexConfig config;
    private final SecretRedactor redactor = new PatternSecretRedactor();

    public DockerLatexCompiler(LatexConfig config) {
        this.config = config;
    }

    @Override
    public Optional<Path> compile(Path texFile) {
        if (texFile == null || !Files.isRegularFile(texFile)
                || config == null || config.dockerImage() == null || config.dockerImage().isBlank()) {
            return Optional.empty();
        }
        Path absolute = texFile.toAbsolutePath();
        String fileName = absolute.getFileName().toString();
        if (!fileName.endsWith(".tex")) return Optional.empty();
        String base = fileName.substring(0, fileName.length() - 4);

        Path outputDir;
        try {
            outputDir = Files.createTempDirectory("latex-out-");
        } catch (IOException cannotCreate) {
            return Optional.empty();
        }
        try {
            List<String> command = List.of("docker", "run", "--rm", "--network", "none", "--read-only",
                    "--cap-drop", "ALL", "--security-opt", "no-new-privileges",
                    "--memory", config.memoryLimit(), "--pids-limit", Integer.toString(config.pidsLimit()),
                    "--user", config.user(),
                    "-v", absolute + ":/doc/" + fileName + ":ro",
                    "-v", outputDir.toAbsolutePath() + ":/out:rw",
                    config.dockerImage(),
                    // L'ENTRYPOINT de l'image fixe deja ces options ; on les repete ici pour ne
                    // pas dependre de son contenu (section 8 : ne jamais faire confiance a l'image).
                    "-interaction=nonstopmode", "-halt-on-error", "-no-shell-escape",
                    "-output-directory=/out", fileName);
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            StringBuilder log = new StringBuilder();
            Thread reader = Thread.ofVirtual().start(() -> read(process.getInputStream(), log));
            boolean completed = process.waitFor(config.timeoutSeconds(), TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                reader.join(1000);
                writeLog(absolute, base, log.toString());
                return Optional.empty();
            }
            reader.join(1000);
            writeLog(absolute, base, log.toString());
            Path producedPdf = outputDir.resolve(base + ".pdf");
            if (process.exitValue() != 0 || !Files.isRegularFile(producedPdf)) {
                return Optional.empty();
            }
            Path pdf = absolute.resolveSibling(base + ".pdf");
            Files.copy(producedPdf, pdf, StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(pdf);
        } catch (IOException | InterruptedException failure) {
            if (failure instanceof InterruptedException) Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            deleteRecursively(outputDir);
        }
    }

    /** Journal de compilation ecrit a cote du .tex, secrets masques (section 12). */
    private void writeLog(Path texFile, String base, String rawLog) {
        try {
            Files.writeString(texFile.resolveSibling(base + ".compile.log"), redactor.redact(rawLog));
        } catch (IOException ignored) {
        }
    }

    private static void read(InputStream stream, StringBuilder target) {
        try (stream) {
            target.append(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    private static void deleteRecursively(Path directory) {
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
