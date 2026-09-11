package ma.uae.aireviewer.application.usecase;

import ma.uae.aireviewer.analysis.AnalysisEngine;
import ma.uae.aireviewer.analysis.criterion.CriterionCatalog;
import ma.uae.aireviewer.analysis.result.AnalysisResult;
import ma.uae.aireviewer.application.dto.RunAnalysisRequest;
import ma.uae.aireviewer.persistence.AnalysisHistoryRepository;
import ma.uae.aireviewer.project.selection.FileSelector;
import ma.uae.aireviewer.analysis.trace.InMemoryTraceRecorder;
import ma.uae.aireviewer.analysis.criterion.CriterionProfile;
import java.time.Instant;
import java.util.UUID;
import ma.uae.aireviewer.persistence.entity.AnalysisRecord;

/** Cas d'utilisation : lancer une analyse et enregistrer son resultat. */
public final class RunAnalysisUseCase {

    private final AnalysisEngine engine;
    private final CriterionCatalog catalog;
    private final FileSelector fileSelector;
    private final AnalysisHistoryRepository history;
    private final AnalysisResultStore results;

    public RunAnalysisUseCase(AnalysisEngine engine,
                              CriterionCatalog catalog,
                              FileSelector fileSelector,
                              AnalysisHistoryRepository history) {
        this(engine, catalog, fileSelector, history, AnalysisResultStore.DEFAULT);
    }

    public RunAnalysisUseCase(AnalysisEngine engine,
                              CriterionCatalog catalog,
                              FileSelector fileSelector,
                              AnalysisHistoryRepository history,
                              AnalysisResultStore results) {
        this.engine = engine;
        this.catalog = catalog;
        this.fileSelector = fileSelector;
        this.history = history;
        this.results = results;
    }

    public AnalysisResult execute(RunAnalysisRequest request) {
        if (request == null || request.profileName() == null || request.profileName().isBlank()) {
            throw new IllegalArgumentException("Profil d'analyse obligatoire");
        }
        var project = ImportProjectUseCase.find(request.projectId());
        if (project == null) throw new IllegalArgumentException("Projet introuvable : " + request.projectId());
        CriterionProfile profile = catalog.profile(request.profileName());
        String id = UUID.randomUUID().toString();
        AnalysisResult result = engine.run(new ma.uae.aireviewer.analysis.AnalysisContext(id, project,
                fileSelector.select(project), profile, new InMemoryTraceRecorder()));
        results.put(result);
        history.save(new AnalysisRecord(id, project.metadata().name(), Instant.now(),
                result.overall().score(), result.overall().maxScore(), result.profileName(),
                result.modelUsed(), null));
        return result;
    }
}
