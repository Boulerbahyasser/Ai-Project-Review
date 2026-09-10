package ma.uae.aireviewer.analysis.analyzer.decorator;

import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.Analyzer;
import ma.uae.aireviewer.analysis.result.CriterionResult;

/**
 * Isole l'echec d'un critere : convertit toute exception en CriterionResult FAILED
 * pour permettre la recuperation partielle de l'analyse (section 5).
 */
public final class FailSafeAnalyzer extends AnalyzerDecorator {

    public FailSafeAnalyzer(Analyzer delegate) {
        super(delegate);
    }

    @Override
    public CriterionResult analyze(AnalysisContext context) {
        try {
            return delegate.analyze(context);
        } catch (RuntimeException exception) {
            return CriterionResult.failed(criterion().id(), criterion().maxScore(), exception.getMessage());
        }
    }
}
