package ma.uae.aireviewer.analysis.result;

import java.time.Instant;
import java.util.List;
import ma.uae.aireviewer.analysis.trace.TraceEvent;
import ma.uae.aireviewer.project.model.ProjectMetadata;

/** Resultat complet d'une analyse : source unique du rapport et de l'historique. */
public record AnalysisResult(
        String analysisId,
        ProjectMetadata project,
        Instant startedAt,
        Instant finishedAt,
        String profileName,
        String modelUsed,
        List<CriterionResult> criterionResults,
        OverallScore overall,
        List<String> errors,
        List<TraceEvent> trace) {

    public AnalysisResult {
        criterionResults = List.copyOf(criterionResults);
        errors = List.copyOf(errors);
        trace = List.copyOf(trace);
    }

    public boolean isPartial() {
        return criterionResults.stream().anyMatch(r -> r.status() != ResultStatus.COMPLETED);
    }
}
