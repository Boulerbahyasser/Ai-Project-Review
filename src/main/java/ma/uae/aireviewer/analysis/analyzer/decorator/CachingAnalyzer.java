package ma.uae.aireviewer.analysis.analyzer.decorator;

import ma.uae.aireviewer.analysis.AnalysisContext;
import ma.uae.aireviewer.analysis.analyzer.Analyzer;
import ma.uae.aireviewer.analysis.result.CriterionResult;
import ma.uae.aireviewer.persistence.cache.CacheStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Evite de reevaluer un critere sur un contenu de projet inchange (section 4.2). */
public final class CachingAnalyzer extends AnalyzerDecorator {

    private final CacheStore cache;

    public CachingAnalyzer(Analyzer delegate, CacheStore cache) {
        super(delegate);
        this.cache = cache;
    }

    @Override
    public CriterionResult analyze(AnalysisContext context) {
        final String key;
        try {
            key = key(context);
        } catch (Exception ignored) {
            return delegate.analyze(context);
        }
        try {
            var cached = cache.get(key);
            if (cached.isPresent()) {
                return new ObjectMapper().readValue(cached.get(), CriterionResult.class);
            }
        } catch (Exception ignored) {
            // A corrupt cache entry is equivalent to a cache miss.
        }
        CriterionResult result = delegate.analyze(context);
        try {
            cache.put(key, new ObjectMapper().writeValueAsString(result));
        } catch (Exception ignored) {
            // Cache persistence is best effort.
        }
        return result;
    }

    private String key(AnalysisContext context) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(criterion().id().value().getBytes(StandardCharsets.UTF_8));
        for (var file : context.selectedFiles()) {
            digest.update(file.path().toString().getBytes(StandardCharsets.UTF_8));
            digest.update(Long.toString(file.sizeInBytes()).getBytes(StandardCharsets.UTF_8));
            try {
                digest.update(java.nio.file.Files.readAllBytes(file.path()));
            } catch (java.io.IOException ignored) {
                // path and size still provide a stable fallback key
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
