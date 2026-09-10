package ma.uae.aireviewer.analysis;

import ma.uae.aireviewer.analysis.aggregation.ScoreAggregator;
import ma.uae.aireviewer.analysis.analyzer.AnalyzerRegistry;
import ma.uae.aireviewer.analysis.result.AnalysisResult;
import ma.uae.aireviewer.application.event.AnalysisEventPublisher;

/**
 * Orchestration : resout les Analyzer du profil, les execute, publie la progression,
 * consolide les scores. Ne connait ni le LLM, ni l'IHM, ni le format de rapport.
 */
public final class DefaultAnalysisEngine implements AnalysisEngine {

    private final AnalyzerRegistry registry;
    private final ScoreAggregator aggregator;
    private final AnalysisEventPublisher events;

    public DefaultAnalysisEngine(AnalyzerRegistry registry,
                                 ScoreAggregator aggregator,
                                 AnalysisEventPublisher events) {
        this.registry = registry;
        this.aggregator = aggregator;
        this.events = events;
    }

    @Override
    public AnalysisResult run(AnalysisContext context) {
        throw new UnsupportedOperationException(
                "TODO : pour chaque critere du profil -> analyzer.analyze(context), "
                        + "publier les evenements, collecter les resultats partiels, agreger");
    }
}
