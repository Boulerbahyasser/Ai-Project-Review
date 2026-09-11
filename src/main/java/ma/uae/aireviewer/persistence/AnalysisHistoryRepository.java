package ma.uae.aireviewer.persistence;

import java.util.List;
import java.util.Optional;
import ma.uae.aireviewer.persistence.entity.AnalysisRecord;

/** Historique des analyses (section 2, point 10). */
public interface AnalysisHistoryRepository {

    void save(AnalysisRecord record);

    List<AnalysisRecord> findAll();

    Optional<AnalysisRecord> findById(String analysisId);
}
