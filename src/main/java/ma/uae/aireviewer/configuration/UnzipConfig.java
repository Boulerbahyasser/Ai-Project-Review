package ma.uae.aireviewer.configuration;

/** Parametres d'isolation Docker pour l'extraction d'archives (section 8). */
public record UnzipConfig(
        String dockerImage,
        String memoryLimit,
        int pidsLimit,
        int timeoutSeconds,
        String user,
        int maxExtractedSizeMb,
        int maxEntryCount) {
}
