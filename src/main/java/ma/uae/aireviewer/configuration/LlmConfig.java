package ma.uae.aireviewer.configuration;

/** Parametres du fournisseur de LLM. Le champ apiKeyEnvVar contient un NOM de variable, pas un secret. */
public record LlmConfig(
        String providerId,
        String model,
        String baseUrl,
        String apiKeyEnvVar,
        int timeoutSeconds,
        int maxRetries,
        int maxOutputTokens,
        double temperature,
        boolean cacheEnabled,
        String fallbackProviderId) {
}
