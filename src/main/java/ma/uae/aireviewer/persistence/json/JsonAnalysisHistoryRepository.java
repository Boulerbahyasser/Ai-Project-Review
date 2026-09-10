package ma.uae.aireviewer.persistence.json;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import ma.uae.aireviewer.persistence.AnalysisHistoryRepository;
import ma.uae.aireviewer.persistence.entity.AnalysisRecord;

/** Historique persiste en fichiers JSON. */
public final class JsonAnalysisHistoryRepository implements AnalysisHistoryRepository {

    private final Path directory;

    public JsonAnalysisHistoryRepository(Path directory) {
        this.directory = directory;
    }

    @Override
    public void save(AnalysisRecord record) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public List<AnalysisRecord> findAll() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public Optional<AnalysisRecord> findById(String analysisId) {
        throw new UnsupportedOperationException("TODO");
    }
}
