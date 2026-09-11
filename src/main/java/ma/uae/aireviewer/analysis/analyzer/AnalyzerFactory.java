package ma.uae.aireviewer.analysis.analyzer;

import ma.uae.aireviewer.analysis.criterion.Criterion;

/**
 * Pattern Factory : construit l'Analyzer correspondant a un critere declare
 * en configuration (deterministe, LLM ou hybride).
 */
public interface AnalyzerFactory {

    boolean supports(Criterion criterion);

    Analyzer create(Criterion criterion);
}
