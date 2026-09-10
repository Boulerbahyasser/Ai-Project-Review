package ma.uae.aireviewer.llm;

import java.util.List;
import ma.uae.aireviewer.configuration.LlmConfig;

/** Selectionne la fabrique correspondant au providerId configure. */
public final class LlmProviderRegistry {

    private final List<LlmProviderFactory> factories;

    public LlmProviderRegistry(List<LlmProviderFactory> factories) {
        this.factories = List.copyOf(factories);
    }

    public List<LlmProviderFactory> factories() {
        return factories;
    }

    public LlmProvider provider(LlmConfig config) {
        return factories.stream()
                .filter(factory -> factory.supports(config.providerId()))
                .findFirst()
                .map(factory -> factory.create(config))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fournisseur de LLM inconnu : " + config.providerId()));
    }
}
