package ma.uae.aireviewer.analysis.analyzer;

import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;

/**
 * Evalue un critere. Unite d'extension principale de l'application :
 * ajouter un critere analysable = ajouter un Analyzer, sans toucher au moteur.
 */
public interface Analyzer {

    Criterion criterion();

    CriterionResult analyze(AnalysisContext context);
}
