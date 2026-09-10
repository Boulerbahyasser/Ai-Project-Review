package ma.uae.aireviewer.report.compile;

import java.nio.file.Path;
import java.util.Optional;

/** Compilation optionnelle .tex -> .pdf. Ne doit pas compromettre la securite. */
public interface PdfCompiler {

    Optional<Path> compile(Path texFile);
}
