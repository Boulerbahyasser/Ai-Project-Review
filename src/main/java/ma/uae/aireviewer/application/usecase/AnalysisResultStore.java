package ma.uae.aireviewer.application.usecase;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ma.uae.aireviewer.analysis.result.AnalysisResult;

/** In-process bridge between analysis and report use cases. */
public final class AnalysisResultStore {
    public static final AnalysisResultStore DEFAULT = new AnalysisResultStore();
    private final Map<String, AnalysisResult> results = new ConcurrentHashMap<>();

    public void put(AnalysisResult result) {
        if (result != null) results.put(result.analysisId(), result);
    }

    public Optional<AnalysisResult> find(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(results.get(id));
    }
}
