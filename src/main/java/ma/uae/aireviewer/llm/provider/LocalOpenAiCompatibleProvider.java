package ma.uae.aireviewer.llm.provider;

import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmRequest;

/**
 * Modele execute localement et expose via une API compatible OpenAI
 * (LM Studio, Ollama). Fournisseur par defaut : aucune donnee du projet
 * analyse ne quitte la machine.
 */
public final class LocalOpenAiCompatibleProvider extends AbstractHttpLlmProvider {

    public LocalOpenAiCompatibleProvider(LlmConfig config) {
        super(config, null);
    }

    @Override
    public String id() {
        return "local";
    }

    @Override
    protected String endpoint() {
        return config.baseUrl() + "/chat/completions";
    }

    @Override
    protected String requestBody(LlmRequest request) {
        throw new UnsupportedOperationException("TODO : corps JSON compatible OpenAI (messages, temperature, response_format)");
    }

    @Override
    protected String extractContent(String responseBody) throws LlmException {
        throw new UnsupportedOperationException("TODO : lire choices[0].message.content");
    }
}
