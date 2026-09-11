package ma.uae.aireviewer.security.archive;

import java.nio.file.Path;

/** Resultat d'une extraction isolee. */
public record ExtractionResult(ExtractionStatus status, Path destination, int exitCode, String message) {

    public boolean success() {
        return status == ExtractionStatus.SUCCESS;
    }
}
