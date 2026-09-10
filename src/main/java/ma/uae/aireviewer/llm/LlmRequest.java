package ma.uae.aireviewer.llm;

/**
 * Requete adressee a un modele, independante de toute API concrete.
 * jsonMode demande une sortie structuree (cahier des charges, section 4.3).
 */
public record LlmRequest(
        String model,
        String systemPrompt,
        String userPrompt,
        double temperature,
        int maxOutputTokens,
        boolean jsonMode) {
}
