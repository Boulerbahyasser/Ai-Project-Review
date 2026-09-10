package ma.uae.aireviewer.report.compile;

import java.nio.file.Path;
import java.util.Optional;

/** Comportement par defaut : le .tex est produit, le PDF n'est pas compile. */
public final class NoOpPdfCompiler implements PdfCompiler {

    @Override
    public Optional<Path> compile(Path texFile) {
        return Optional.empty();
    }
}
