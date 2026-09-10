package ma.uae.aireviewer.llm.provider;

import ma.uae.aireviewer.configuration.ConfigurationException;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.configuration.SecretProvider;
import ma.uae.aireviewer.llm.LlmProvider;
import ma.uae.aireviewer.llm.LlmProviderFactory;

/** DeepSeek : meme exigence de cle que Mistral. */
public final class DeepSeekProviderFactory implements LlmProviderFactory {

    public static final String PROVIDER_ID = "deepseek";

    private final SecretProvider secrets;

    public DeepSeekProviderFactory(SecretProvider secrets) {
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
                        "Le fournisseur deepseek requiert une cle : definissez la variable "
                                + "d'environnement " + config.apiKeyEnvVar()));
        return new DeepSeekProvider(config, apiKey);
    }
}
