package ma.uae.aireviewer.llm;

import java.util.List;
import ma.uae.aireviewer.configuration.ConfigurationException;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.cache.CachingLlmProvider;
import ma.uae.aireviewer.llm.resilience.FallbackLlmProvider;
import ma.uae.aireviewer.llm.resilience.RetryPolicy;
import ma.uae.aireviewer.llm.resilience.RetryingLlmProvider;
import ma.uae.aireviewer.persistence.cache.CacheStore;

/**
 * Point d'entree unique de construction d'un {@link LlmProvider}.
 *
 * <p>Deux responsabilites separees : les fabriques savent <b>quel dialecte</b>
 * parler, l'assembleur decide <b>quels comportements transverses</b> ajouter.
 * La chaine de decorateurs n'existe donc qu'ici, et une seule fois dans tout le
 * programme.
 *
 * <p>Ordre d'empilement, et il n'est pas indifferent :
 * <pre>
 *   cache( reprise( repli( transport ) ) )
 * </pre>
 * Le repli au plus pres du transport, pour que basculer de fournisseur soit
 * lui-meme rejouable ; la reprise au-dessus ; le cache en dernier, afin qu'un
 * succes de cache court-circuite tout le reste. Placer le cache a l'interieur
 * ferait memoriser des reponses avant validation.
 */
public final class LlmProviderAssembler {

    private final List<LlmProviderFactory> factories;
    private final CacheStore cache;

    public LlmProviderAssembler(List<LlmProviderFactory> factories, CacheStore cache) {
        this.factories = List.copyOf(factories);
        this.cache = cache;
    }

    /** Fournisseur complet, decore selon la configuration. */
    public LlmProvider assemble(LlmConfig config) {
        LlmProvider provider = withFallback(config);
        provider = withRetry(provider, config);
        return withCache(provider, config);
    }

    /**
     * Transport nu, sans aucun decorateur. Utile pour un diagnostic ou un essai
     * de connexion, ou tant que les decorateurs de resilience ne sont pas ecrits.
     */
    public LlmProvider transportOnly(LlmConfig config) {
        return factoryFor(config.providerId()).create(config);
    }

    // ---------------------------------------------------------------- couches

    private LlmProvider withFallback(LlmConfig config) {
        LlmProvider primary = transportOnly(config);
        if (!config.hasFallback()) {
            return primary;
        }
        if (config.fallbackProviderId().equals(config.providerId())) {
            throw new ConfigurationException(
                    "llm.fallbackProviderId doit differer de llm.providerId");
        }
        // Le repli reutilise la meme configuration, en changeant d'identifiant :
        // baseUrl et cle propres au second fournisseur restent a fournir par
        // configuration si les deux ne partagent pas la meme.
        LlmProvider secondary = factoryFor(config.fallbackProviderId()).create(config);
        return new FallbackLlmProvider(primary, secondary);
    }

    private LlmProvider withRetry(LlmProvider provider, LlmConfig config) {
        // maxRetries == 1 signifie « une seule tentative » : le decorateur
        // n'apporterait rien et n'est donc pas empile.
        return config.maxRetries() > 1
                ? new RetryingLlmProvider(provider, RetryPolicy.defaults(config.maxRetries()))
                : provider;
    }

    private LlmProvider withCache(LlmProvider provider, LlmConfig config) {
        if (!config.cacheEnabled()) {
            return provider;
        }
        if (cache == null) {
            throw new ConfigurationException(
                    "llm.cacheEnabled vaut true mais aucun CacheStore n'a ete fourni");
        }
        return new CachingLlmProvider(provider, cache);
    }

    private LlmProviderFactory factoryFor(String providerId) {
        return factories.stream()
                .filter(factory -> factory.supports(providerId))
                .findFirst()
                .orElseThrow(() -> new ConfigurationException(
                        "Fournisseur de LLM inconnu : '" + providerId + "'. Connus : "
                                + knownProviders()));
    }

    private String knownProviders() {
        return factories.isEmpty() ? "aucun" : String.valueOf(factories.size()) + " enregistre(s)";
    }
}
