package ma.uae.aireviewer.configuration;

/**
 * Parametres du fournisseur de modele.
 *
 * <p>{@code apiKeyEnvVar} contient le NOM d'une variable d'environnement, jamais
 * une cle : aucun secret ne doit figurer dans le depot (section 17).
 *
 * <p>{@code maxCharsPerRequest} et {@code maxFilesPerRequest} decrivent la fenetre
 * de contexte du modele. Ils appartiennent bien a cette configuration et non a
 * celle de l'analyse : ce sont des proprietes du modele, pas du projet evalue.
 */
public record LlmConfig(
        String providerId,
        String model,
        String baseUrl,
        String apiKeyEnvVar,
        int timeoutSeconds,
        int maxRetries,
        int maxOutputTokens,
        double temperature,
        int maxCharsPerRequest,
        int maxFilesPerRequest,
        boolean cacheEnabled,
        String fallbackProviderId) {

    public LlmConfig {
        requirePositive(timeoutSeconds, "llm.timeoutSeconds");
        requirePositive(maxRetries, "llm.maxRetries");
        requirePositive(maxOutputTokens, "llm.maxOutputTokens");
        requirePositive(maxCharsPerRequest, "llm.maxCharsPerRequest");
        requirePositive(maxFilesPerRequest, "llm.maxFilesPerRequest");
        requireText(providerId, "llm.providerId");
        requireText(model, "llm.model");
        requireText(baseUrl, "llm.baseUrl");
        if (temperature < 0 || temperature > 2) {
            throw new ConfigurationException("llm.temperature doit etre entre 0 et 2, recu " + temperature);
        }
        // Une barre oblique finale produirait une URL a double separateur.
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /** Vrai si un second fournisseur est declare comme repli (section 5). */
    public boolean hasFallback() {
        return fallbackProviderId != null && !fallbackProviderId.isBlank();
    }

    private static void requirePositive(int value, String key) {
        if (value <= 0) {
            throw new ConfigurationException(key + " doit etre strictement positif, recu " + value);
        }
    }

    private static void requireText(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(key + " est obligatoire");
        }
    }
}
