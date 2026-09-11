package ma.uae.aireviewer.application;

import java.nio.file.Path;
import java.util.List;
import ma.uae.aireviewer.application.dto.AnalysisSummary;
import ma.uae.aireviewer.application.dto.GenerateReportRequest;
import ma.uae.aireviewer.application.dto.ImportProjectRequest;
import ma.uae.aireviewer.application.dto.RunAnalysisRequest;
import ma.uae.aireviewer.application.event.AnalysisEventPublisher;
import ma.uae.aireviewer.application.event.AnalysisListener;
import ma.uae.aireviewer.application.usecase.GenerateReportUseCase;
import ma.uae.aireviewer.application.usecase.ImportProjectUseCase;
import ma.uae.aireviewer.application.usecase.RunAnalysisUseCase;
import ma.uae.aireviewer.application.usecase.ViewHistoryUseCase;
import ma.uae.aireviewer.persistence.entity.AnalysisRecord;
import ma.uae.aireviewer.project.model.SoftwareProject;
import ma.uae.aireviewer.analysis.criterion.CriterionCatalog;
import ma.uae.aireviewer.analysis.result.AnalysisResult;

/** Delegue chaque appel au cas d'utilisation correspondant. */
public final class DefaultAiReviewerFacade implements AiReviewerFacade {

    private final ImportProjectUseCase importProject;
    private final RunAnalysisUseCase runAnalysis;
    private final GenerateReportUseCase generateReport;
    private final ViewHistoryUseCase viewHistory;
    private final AnalysisEventPublisher events;
    private final CriterionCatalog catalog;

    public DefaultAiReviewerFacade(ImportProjectUseCase importProject,
                                   RunAnalysisUseCase runAnalysis,
                                   GenerateReportUseCase generateReport,
                                   ViewHistoryUseCase viewHistory,
                                   AnalysisEventPublisher events) {
        this(importProject, runAnalysis, generateReport, viewHistory, events, null);
    }

    public DefaultAiReviewerFacade(ImportProjectUseCase importProject,
                                   RunAnalysisUseCase runAnalysis,
                                   GenerateReportUseCase generateReport,
                                   ViewHistoryUseCase viewHistory,
                                   AnalysisEventPublisher events,
                                   CriterionCatalog catalog) {
        this.importProject = importProject;
        this.runAnalysis = runAnalysis;
        this.generateReport = generateReport;
        this.viewHistory = viewHistory;
        this.events = events;
        this.catalog = catalog;
    }

    @Override
    public SoftwareProject importProject(ImportProjectRequest request) {
        return importProject.execute(request);
    }

    @Override
    public List<String> availableProfiles() {
        return catalog == null ? List.of() : catalog.profileNames();
    }

    @Override
    public AnalysisSummary runAnalysis(RunAnalysisRequest request) {
        AnalysisResult result = runAnalysis.execute(request);
        return new AnalysisSummary(result.analysisId(), result.project().name(),
                result.overall().score(), result.overall().maxScore(), result.isPartial(),
                (int) result.criterionResults().stream()
                        .filter(item -> item.status() == ma.uae.aireviewer.analysis.result.ResultStatus.FAILED)
                        .count());
    }

    @Override
    public Path generateReport(GenerateReportRequest request) {
        return generateReport.execute(request);
    }

    @Override
    public List<AnalysisRecord> history() {
        return viewHistory.execute();
    }

    @Override
    public void subscribe(AnalysisListener listener) {
        events.subscribe(listener);
    }
}
