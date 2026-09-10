package ma.uae.aireviewer.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.TokenUsage;

/**
 * Fournisseur distant DeepSeek ({@code https://api.deepseek.com/v1}).
 *
 * <p>Egalement au format compatible OpenAI. Cette classe existe pour demontrer que
 * l'ajout d'un troisieme fournisseur ne coute qu'une classe et une fabrique, sans
 * modifier aucun composant existant.
 */
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
    protected ObjectNode requestBody(LlmRequest request) {
        return OpenAiChatFormat.requestBody(request, json.createObjectNode());
    }

    @Override
    protected String extractContent(JsonNode response) throws LlmException {
        return OpenAiChatFormat.content(response);
    }

    @Override
    protected TokenUsage extractUsage(JsonNode response) {
        return OpenAiChatFormat.usage(response);
    }

    @Override
    protected String servedModel(JsonNode response, String requested) {
        return OpenAiChatFormat.servedModel(response, requested);
    }
}
