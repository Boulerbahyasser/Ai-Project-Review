package ma.uae.aireviewer.security.archive;

import java.nio.file.Path;

/** Implementation par defaut en l'absence de Docker : ne tente aucune extraction. */
public final class DisabledArchiveExtractor implements ArchiveExtractor {

    @Override
    public ExtractionResult extract(Path archive, Path destination) {
        return new ExtractionResult(ExtractionStatus.DOCKER_UNAVAILABLE, destination, -1,
                "Extraction desactivee : aucun moteur Docker disponible");
    }
}
