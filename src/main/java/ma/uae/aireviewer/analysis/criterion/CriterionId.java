package ma.uae.aireviewer.analysis.criterion;

/** Identifiant stable d'un critere (utilise en configuration, cache et rapport). */
public record CriterionId(String value) {

    public CriterionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CriterionId ne peut pas etre vide");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
