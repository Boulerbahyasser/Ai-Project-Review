package ma.uae.aireviewer.analysis.criterion;

/**
 * Critere d'evaluation declare en configuration (cahier des charges, section 3.3).
 * Ajouter un critere ne modifie aucun composant existant : une entree dans le profil
 * plus, le cas echeant, un Analyzer dedie enregistre dans AnalyzerRegistry.
 */
public record Criterion(
        CriterionId id,
        String label,
        String description,
        int maxScore,
        double weight,
        AnalysisKind kind) {
}
