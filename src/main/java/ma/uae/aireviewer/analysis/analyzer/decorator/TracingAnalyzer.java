package ma.uae.aireviewer.analysis.analyzer.decorator;

import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.Analyzer;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.analysis.trace.TraceEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/** Enregistre debut, fin et duree de l'evaluation dans la trace (section 12). */
public final class TracingAnalyzer extends AnalyzerDecorator {

    public TracingAnalyzer(Analyzer delegate) {
        super(delegate);
    }

    @Override
    public CriterionResult analyze(AnalysisContext context) {
        Instant started = Instant.now();
        if (context.trace() != null) {
            context.trace().record(new TraceEvent(started, "analyzer.start",
                    criterion().id().toString(), Duration.ZERO, Map.of()));
        }
        try {
            return delegate.analyze(context);
        } finally {
            if (context.trace() != null) {
                context.trace().record(new TraceEvent(Instant.now(), "analyzer.finish",
                        criterion().id().toString(), Duration.between(started, Instant.now()), Map.of()));
            }
        }
    }
}
