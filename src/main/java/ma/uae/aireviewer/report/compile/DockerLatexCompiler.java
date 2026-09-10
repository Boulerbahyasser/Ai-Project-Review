package ma.uae.aireviewer.report.compile;

import java.nio.file.Path;
import java.util.Optional;

/** Compilation isolee dans un conteneur, sans shell-escape. */
public final class DockerLatexCompiler implements PdfCompiler {

    private final String image;

    public DockerLatexCompiler(String image) {
        this.image = image;
    }

    @Override
    public Optional<Path> compile(Path texFile) {
        throw new UnsupportedOperationException("TODO");
    }
}
