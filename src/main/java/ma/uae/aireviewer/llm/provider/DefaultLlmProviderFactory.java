package ma.uae.aireviewer.llm.provider;

import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.configuration.SecretProvider;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmProviderFactory;
import ma.uae.aireviewer.persistence.cache.CacheStore;
import ma.uae.aireviewer.llm.resilience.RetryPolicy;
import ma.uae.aireviewer.llm.resilience.RetryingLlmProvider;
import ma.uae.aireviewer.llm.cache.CachingLlmProvider;

/**
 * Assemble le fournisseur configure et l'habille de ses decorateurs :
 * transport concret -> reprise sur erreur -> cache.
 */
public final class DefaultLlmProviderFactory implements LlmProviderFactory {

    private final SecretProvider secrets;
    private final CacheStore cache;

    public DefaultLlmProviderFactory(SecretProvider secrets, CacheStore cache) {
        this.secrets = secrets;
        this.cache = cache;
    }

    @Override
    public boolean supports(String providerId) {
        return switch (providerId) {
            case "local", "mistral", "deepseek" -> true;
            default -> false;
        };
    }

    @Override
    public LlmProvider create(LlmConfig config) {
        LlmProvider transport = transportFor(config);
        LlmProvider resilient = new RetryingLlmProvider(transport, RetryPolicy.defaults(config.maxRetries()));
        return config.cacheEnabled() ? new CachingLlmProvider(resilient, cache) : resilient;
    }

    private LlmProvider transportFor(LlmConfig config) {
        String apiKey = secrets.secret(config.apiKeyEnvVar()).orElse(null);
        return switch (config.providerId()) {
            case "local" -> new LocalOpenAiCompatibleProvider(config);
            case "mistral" -> new MistralProvider(config, apiKey);
            case "deepseek" -> new DeepSeekProvider(config, apiKey);
            default -> throw new IllegalArgumentException("Fournisseur inconnu : " + config.providerId());
        };
    }
}
