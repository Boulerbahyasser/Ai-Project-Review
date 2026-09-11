package ma.uae.aireviewer.analysis.analyzer;

import java.util.List;
import ma.uae.aireviewer.analysis.criterion.Criterion;
import ma.uae.aireviewer.analysis.criterion.CriterionProfile;

/**
 * Resout les Analyzer d'un profil via les fabriques enregistrees, puis applique
 * la chaine de decorateurs (trace, tolerance aux pannes, cache).
 */
public final class AnalyzerRegistry {

    private final List<AnalyzerFactory> factories;

    public AnalyzerRegistry(List<AnalyzerFactory> factories) {
        this.factories = List.copyOf(factories);
    }

    public Analyzer analyzerFor(Criterion criterion) {
        return factories.stream()
                .filter(factory -> factory.supports(criterion))
                .findFirst()
                .map(factory -> factory.create(criterion))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucune fabrique d'Analyzer pour le critere : " + criterion.id()));
    }

    public List<Analyzer> analyzersFor(CriterionProfile profile) {
        return profile.criteria().stream().map(this::analyzerFor).toList();
    }
}
