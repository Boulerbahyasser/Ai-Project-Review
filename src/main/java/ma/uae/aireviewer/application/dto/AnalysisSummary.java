package ma.uae.aireviewer.application.dto;

/** Vue simplifiee d'un resultat, destinee a l'affichage. */
public record AnalysisSummary(
        String analysisId,
        String projectName,
        double score,
        double maxScore,
        boolean partial,
        int failedCriteria) {
}
