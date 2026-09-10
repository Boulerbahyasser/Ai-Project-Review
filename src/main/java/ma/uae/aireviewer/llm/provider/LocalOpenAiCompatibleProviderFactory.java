package ma.uae.aireviewer.llm.provider;

import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmProviderFactory;

/** Modele local : aucune cle d'API n'est requise ni consultee. */
public final class LocalOpenAiCompatibleProviderFactory implements LlmProviderFactory {

    public static final String PROVIDER_ID = "local";

    @Override
    public boolean supports(String providerId) {
        return PROVIDER_ID.equals(providerId);
    }

    @Override
    public LlmProvider create(LlmConfig config) {
        return new LocalOpenAiCompatibleProvider(config);
    }
}
