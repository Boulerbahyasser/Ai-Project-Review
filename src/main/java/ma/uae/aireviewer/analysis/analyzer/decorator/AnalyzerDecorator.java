package ma.uae.aireviewer.analysis.analyzer.decorator;

import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.Analyzer;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.result.CriterionResult;

/**
 * Pattern Decorator : base des comportements ajoutes dynamiquement a un Analyzer
 * (trace, tolerance aux pannes, cache) sans modifier les analyzers existants.
 */
public abstract class AnalyzerDecorator implements Analyzer {

    protected final Analyzer delegate;

    protected AnalyzerDecorator(Analyzer delegate) {
        this.delegate = delegate;
    }

    @Override
    public Criterion criterion() {
        return delegate.criterion();
    }

    @Override
    public CriterionResult analyze(AnalysisContext context) {
        return delegate.analyze(context);
    }
}
