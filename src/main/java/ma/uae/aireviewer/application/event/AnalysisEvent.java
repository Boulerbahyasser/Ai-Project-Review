package ma.uae.aireviewer.application.event;

import ma.uae.aireviewer.analysis.criterion.CriterionId;

/** Evenements de progression remontes a l'IHM (pattern Observer). */
public sealed interface AnalysisEvent {

    record Started(String analysisId, int criterionCount) implements AnalysisEvent {}

    record CriterionStarted(CriterionId criterion) implements AnalysisEvent {}

    record CriterionCompleted(CriterionId criterion, int score, int maxScore) implements AnalysisEvent {}

    record CriterionFailed(CriterionId criterion, String message) implements AnalysisEvent {}

    record Warning(String message) implements AnalysisEvent {}

    record Completed(String analysisId, double score, double maxScore) implements AnalysisEvent {}

    record Failed(String analysisId, String message) implements AnalysisEvent {}
}
