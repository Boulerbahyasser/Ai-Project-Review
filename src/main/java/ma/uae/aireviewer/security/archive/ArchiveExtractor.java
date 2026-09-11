package ma.uae.aireviewer.security.archive;

import java.nio.file.Path;

/** Extraction d'une archive non fiable. Jamais directement sur la machine hote. */
public interface ArchiveExtractor {

    ExtractionResult extract(Path archive, Path destination);
}
