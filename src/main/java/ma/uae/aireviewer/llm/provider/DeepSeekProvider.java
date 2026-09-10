package ma.uae.aireviewer.llm.provider;

import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmRequest;

/** Fournisseur distant DeepSeek. Illustre l'ajout d'un fournisseur sans impact ailleurs. */
public final class DeepSeekProvider extends AbstractHttpLlmProvider {

    public DeepSeekProvider(LlmConfig config, String apiKey) {
        super(config, apiKey);
    }

    @Override
    public String id() {
        return "deepseek";
    }

    @Override
    protected String endpoint() {
        return config.baseUrl() + "/chat/completions";
    }

    @Override
    protected String requestBody(LlmRequest request) {
        throw new UnsupportedOperationException("TODO : corps JSON de l'API DeepSeek");
    }

    @Override
    protected String extractContent(String responseBody) throws LlmException {
        throw new UnsupportedOperationException("TODO : extraire le contenu de la reponse DeepSeek");
    }
}
