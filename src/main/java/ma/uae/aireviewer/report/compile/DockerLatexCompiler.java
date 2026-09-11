package ma.uae.aireviewer.report.compile;

import java.nio.file.Path;
import java.util.Optional;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Compilation isolee dans un conteneur, sans shell-escape. */
public final class DockerLatexCompiler implements PdfCompiler {

    private final String image;

    public DockerLatexCompiler(String image) {
        this.image = image;
    }

    @Override
    public Optional<Path> compile(Path texFile) {
        if (texFile == null || !java.nio.file.Files.isRegularFile(texFile)
                || image == null || image.isBlank()) {
            return Optional.empty();
        }
        Path absolute = texFile.toAbsolutePath();
        String fileName = absolute.getFileName().toString();
        if (!fileName.endsWith(".tex")) return Optional.empty();
        try {
            Process process = new ProcessBuilder("docker", "run", "--rm", "--network", "none",
                    "--read-only", "-v", absolute.getParent() + ":/work",
                    "-w", "/work", image, "pdflatex", "-interaction=nonstopmode", fileName)
                    .redirectErrorStream(true).start();
            process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            if (!process.waitFor(Duration.ofMinutes(2).toMillis(), TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                return Optional.empty();
            }
            Path pdf = absolute.resolveSibling(fileName.substring(0, fileName.length() - 4) + ".pdf");
            return process.exitValue() == 0 && java.nio.file.Files.isRegularFile(pdf)
                    ? Optional.of(pdf) : Optional.empty();
        } catch (IOException | InterruptedException failure) {
            if (failure instanceof InterruptedException) Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
}
