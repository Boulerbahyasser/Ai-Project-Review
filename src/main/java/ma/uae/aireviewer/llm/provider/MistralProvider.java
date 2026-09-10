package ma.uae.aireviewer.llm.provider;

import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmRequest;

/** Fournisseur distant Mistral. Cle lue dans l'environnement, jamais dans le depot. */
public final class MistralProvider extends AbstractHttpLlmProvider {

    public MistralProvider(LlmConfig config, String apiKey) {
        super(config, apiKey);
    }

    @Override
    public String id() {
        return "mistral";
    }

    @Override
    protected String endpoint() {
        return config.baseUrl() + "/chat/completions";
    }

    @Override
    protected String requestBody(LlmRequest request) {
        throw new UnsupportedOperationException("TODO : corps JSON de l'API Mistral");
    }

    @Override
    protected String extractContent(String responseBody) throws LlmException {
        throw new UnsupportedOperationException("TODO : extraire le contenu de la reponse Mistral");
    }
}
