package ma.uae.aireviewer.llm.parsing;

import java.util.List;

/**
 * Contrat de sortie attendu du modele (cahier des charges, section 4.3).
 * Frontiere entre le texte produit par le LLM et le modele metier :
 * CriterionResult n'est construit qu'apres validation de cette structure.
 */
public record EvaluationPayload(
        String criterion,
        int score,
        int maxScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations) {
}
