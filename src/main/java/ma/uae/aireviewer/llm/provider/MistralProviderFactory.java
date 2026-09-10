package ma.uae.aireviewer.llm.provider;

import ma.uae.aireviewer.configuration.ConfigurationException;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.configuration.SecretProvider;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmProviderFactory;

/**
 * Mistral : la cle est obligatoire, et son absence est signalee ici, au moment de
 * la construction — pas au premier appel HTTP, ou l'erreur serait un 401 obscur.
 */
public final class MistralProviderFactory implements LlmProviderFactory {

    public static final String PROVIDER_ID = "mistral";

    private final SecretProvider secrets;

    public MistralProviderFactory(SecretProvider secrets) {
        this.secrets = secrets;
    }

    @Override
    public boolean supports(String providerId) {
        return PROVIDER_ID.equals(providerId);
    }

    @Override
    public LlmProvider create(LlmConfig config) {
        String apiKey = secrets.secret(config.apiKeyEnvVar())
                .orElseThrow(() -> new ConfigurationException(
                        "Le fournisseur mistral requiert une cle : definissez la variable "
                                + "d'environnement " + config.apiKeyEnvVar()));
        return new MistralProvider(config, apiKey);
    }
}
