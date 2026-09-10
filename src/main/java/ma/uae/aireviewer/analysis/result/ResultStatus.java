package ma.uae.aireviewer.analysis.result;

/** Etat d'evaluation d'un critere : permet la recuperation partielle d'une analyse. */
public enum ResultStatus {
    COMPLETED,
    PARTIAL,
    FAILED,
    SKIPPED
}
