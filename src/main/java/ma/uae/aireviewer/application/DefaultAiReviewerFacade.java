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

/** Delegue chaque appel au cas d'utilisation correspondant. */
public final class DefaultAiReviewerFacade implements AiReviewerFacade {

    private final ImportProjectUseCase importProject;
    private final RunAnalysisUseCase runAnalysis;
    private final GenerateReportUseCase generateReport;
    private final ViewHistoryUseCase viewHistory;
    private final AnalysisEventPublisher events;

    public DefaultAiReviewerFacade(ImportProjectUseCase importProject,
                                   RunAnalysisUseCase runAnalysis,
                                   GenerateReportUseCase generateReport,
                                   ViewHistoryUseCase viewHistory,
                                   AnalysisEventPublisher events) {
        this.importProject = importProject;
        this.runAnalysis = runAnalysis;
        this.generateReport = generateReport;
        this.viewHistory = viewHistory;
        this.events = events;
    }

    @Override
    public SoftwareProject importProject(ImportProjectRequest request) {
        return importProject.execute(request);
    }

    @Override
    public List<String> availableProfiles() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public AnalysisSummary runAnalysis(RunAnalysisRequest request) {
        throw new UnsupportedOperationException("TODO");
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
