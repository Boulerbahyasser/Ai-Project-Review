package ma.uae.aireviewer.application.usecase;

import java.util.List;
import ma.uae.aireviewer.persistence.AnalysisHistoryRepository;
import ma.uae.aireviewer.persistence.entity.AnalysisRecord;

/** Cas d'utilisation : consulter l'historique des analyses. */
public final class ViewHistoryUseCase {

    private final AnalysisHistoryRepository history;

    public ViewHistoryUseCase(AnalysisHistoryRepository history) {
        this.history = history;
    }

    public List<AnalysisRecord> execute() {
        return history.findAll();
    }
}
