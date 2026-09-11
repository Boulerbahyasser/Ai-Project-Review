package ma.uae.aireviewer.analysis;

import ma.uae.aireviewer.analysis.aggregation.ScoreAggregator;
import ma.uae.aireviewer.analysis.analyzer.AnalyzerRegistry;
import ma.uae.aireviewer.analysis.result.AnalysisResult;
import ma.uae.aireviewer.application.event.AnalysisEventPublisher;
import ma.uae.aireviewer.application.event.AnalysisEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import ma.uae.aireviewer.analysis.analyzer.Analyzer;
import ma.uae.aireviewer.analysis.result.CriterionResult;

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
        Instant started = Instant.now();
        List<CriterionResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        events.publish(new AnalysisEvent.Started(context.analysisId(), context.profile().criteria().size()));
        for (var declared : context.profile().criteria()) {
            Analyzer analyzer;
            try {
                analyzer = registry.analyzerFor(declared);
            } catch (RuntimeException missingAnalyzer) {
                String message = missingAnalyzer.getMessage() == null
                        ? missingAnalyzer.toString() : missingAnalyzer.getMessage();
                results.add(CriterionResult.failed(declared.id(), declared.maxScore(), message));
                errors.add(declared.id() + ": " + message);
                events.publish(new AnalysisEvent.CriterionFailed(declared.id(), message));
                continue;
            }
            var criterion = analyzer.criterion();
            events.publish(new AnalysisEvent.CriterionStarted(criterion.id()));
            try {
                CriterionResult result = analyzer.analyze(context);
                results.add(result);
                if (result.status() == ma.uae.aireviewer.analysis.result.ResultStatus.FAILED) {
                    errors.add(criterion.id() + ": " + result.errorMessage());
                    events.publish(new AnalysisEvent.CriterionFailed(criterion.id(), result.errorMessage()));
                } else {
                    events.publish(new AnalysisEvent.CriterionCompleted(
                            criterion.id(), result.score(), result.maxScore()));
                }
            } catch (RuntimeException failure) {
                String message = failure.getMessage() == null ? failure.toString() : failure.getMessage();
                results.add(CriterionResult.failed(criterion.id(), criterion.maxScore(), message));
                errors.add(criterion.id() + ": " + message);
                events.publish(new AnalysisEvent.CriterionFailed(criterion.id(), message));
            }
        }
        var overall = aggregator.aggregate(results, context.profile());
        Instant finished = Instant.now();
        events.publish(new AnalysisEvent.Completed(context.analysisId(), overall.score(), overall.maxScore()));
        return new AnalysisResult(context.analysisId(), context.project().metadata(), started, finished,
                context.profile().name(), "unknown", results, overall, errors,
                context.trace() == null ? List.of() : context.trace().events());
    }
}
