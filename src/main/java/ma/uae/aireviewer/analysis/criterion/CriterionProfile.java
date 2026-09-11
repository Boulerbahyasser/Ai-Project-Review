package ma.uae.aireviewer.analysis.criterion;

import java.util.List;
import java.util.Optional;

/** Ensemble nomme de criteres selectionnable depuis l'IHM. */
public record CriterionProfile(String name, List<Criterion> criteria) {

    public CriterionProfile {
        criteria = List.copyOf(criteria);
    }

    public Optional<Criterion> find(CriterionId id) {
        return criteria.stream().filter(criterion -> criterion.id().equals(id)).findFirst();
    }

    public double totalWeightedMax() {
        return criteria.stream().mapToDouble(c -> c.maxScore() * c.weight()).sum();
    }
}
