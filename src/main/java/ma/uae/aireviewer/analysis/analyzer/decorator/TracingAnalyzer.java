package ma.uae.aireviewer.analysis.analyzer.decorator;

import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.Analyzer;
import ma.uae.aireviewer.analysis.result.CriterionResult;

/** Enregistre debut, fin et duree de l'evaluation dans la trace (section 12). */
public final class TracingAnalyzer extends AnalyzerDecorator {

    public TracingAnalyzer(Analyzer delegate) {
        super(delegate);
    }

    @Override
    public CriterionResult analyze(AnalysisContext context) {
        throw new UnsupportedOperationException("TODO : mesurer la duree et alimenter context.trace()");
    }
}
