package ma.uae.aireviewer.application.usecase;

import ma.uae.aireviewer.analysis.AnalysisEngine;
import ma.uae.aireviewer.analysis.criterion.CriterionCatalog;
import ma.uae.aireviewer.analysis.result.AnalysisResult;
import ma.uae.aireviewer.application.dto.RunAnalysisRequest;
import ma.uae.aireviewer.persistence.AnalysisHistoryRepository;
import ma.uae.aireviewer.project.selection.FileSelector;

/** Cas d'utilisation : lancer une analyse et enregistrer son resultat. */
public final class RunAnalysisUseCase {

    private final AnalysisEngine engine;
    private final CriterionCatalog catalog;
    private final FileSelector fileSelector;
    private final AnalysisHistoryRepository history;

    public RunAnalysisUseCase(AnalysisEngine engine,
                              CriterionCatalog catalog,
                              FileSelector fileSelector,
                              AnalysisHistoryRepository history) {
        this.engine = engine;
        this.catalog = catalog;
        this.fileSelector = fileSelector;
        this.history = history;
    }

    public AnalysisResult execute(RunAnalysisRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
