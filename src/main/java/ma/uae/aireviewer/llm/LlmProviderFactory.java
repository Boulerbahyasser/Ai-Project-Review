package ma.uae.aireviewer.llm;

import ma.uae.aireviewer.configuration.LlmConfig;

/**
 * Pattern Abstract Factory : construit un fournisseur a partir de la configuration,
 * decorateurs de resilience et de cache inclus. Le reste de l'application ne
 * connait que LlmProvider.
 */
public interface LlmProviderFactory {

    boolean supports(String providerId);

    LlmProvider create(LlmConfig config);
}
