package ma.uae.aireviewer.persistence.entity;

import java.time.Instant;

/** Entree d'historique. Aucun secret ne doit y figurer. */
public record AnalysisRecord(
        String analysisId,
        String projectName,
        Instant date,
        double score,
        double maxScore,
        String profileName,
        String modelUsed,
        String reportPath) {
}
