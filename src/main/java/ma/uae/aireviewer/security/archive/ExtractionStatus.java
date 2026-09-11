package ma.uae.aireviewer.security.archive;

/** Issue possible d'une extraction isolee. */
public enum ExtractionStatus {
    SUCCESS,
    INVALID_ARCHIVE,
    LIMIT_EXCEEDED,
    TIMEOUT,
    DOCKER_UNAVAILABLE
}
