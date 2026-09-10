package ma.uae.aireviewer.analysis.analyzer.decorator;

import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.Analyzer;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.persistence.cache.CacheStore;

/** Evite de reevaluer un critere sur un contenu de projet inchange (section 4.2). */
public final class CachingAnalyzer extends AnalyzerDecorator {

    private final CacheStore cache;

    public CachingAnalyzer(Analyzer delegate, CacheStore cache) {
        super(delegate);
        this.cache = cache;
    }

    @Override
    public CriterionResult analyze(AnalysisContext context) {
        throw new UnsupportedOperationException(
                "TODO : cle = critere + empreinte des fichiers selectionnes ; lire puis alimenter le cache");
    }
}
