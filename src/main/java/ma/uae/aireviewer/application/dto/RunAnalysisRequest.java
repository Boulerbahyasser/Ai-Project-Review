package ma.uae.aireviewer.application.dto;

/** Demande d'analyse venant de l'IHM. */
public record RunAnalysisRequest(String projectId, String profileName, String providerId) {
}
