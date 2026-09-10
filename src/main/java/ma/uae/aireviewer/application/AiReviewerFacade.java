package ma.uae.aireviewer.application;

import java.nio.file.Path;
import java.util.List;
import ma.uae.aireviewer.application.dto.AnalysisSummary;
import ma.uae.aireviewer.application.dto.GenerateReportRequest;
import ma.uae.aireviewer.application.dto.ImportProjectRequest;
import ma.uae.aireviewer.application.dto.RunAnalysisRequest;
import ma.uae.aireviewer.application.event.AnalysisListener;
import ma.uae.aireviewer.persistence.entity.AnalysisRecord;
import ma.uae.aireviewer.project.model.SoftwareProject;

/**
 * Pattern Facade : unique point d'entree de l'IHM sur le moteur.
 * L'IHM ne connait aucun autre type interne.
 */
public interface AiReviewerFacade {

    SoftwareProject importProject(ImportProjectRequest request);

    List<String> availableProfiles();

    AnalysisSummary runAnalysis(RunAnalysisRequest request);

    Path generateReport(GenerateReportRequest request);

    List<AnalysisRecord> history();

    void subscribe(AnalysisListener listener);
}
