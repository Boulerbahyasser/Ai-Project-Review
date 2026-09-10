package ma.uae.aireviewer.analysis.criterion;

/** Nature de l'analyse d'un critere : deterministe, par LLM, ou combinaison des deux. */
public enum AnalysisKind {
    DETERMINISTIC,
    LLM,
    HYBRID
}
