package ma.uae.aireviewer.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.TokenUsage;

/**
 * Fournisseur distant Mistral ({@code https://api.mistral.ai/v1}).
 *
 * <p>Son API suit le format compatible OpenAI : la traduction est donc entierement
 * deleguee a {@link OpenAiChatFormat}. Seuls l'URL et l'authentification par cle
 * distinguent cette classe du fournisseur local — ce qui illustre le cout reel
 * d'ajout d'un fournisseur (section 10).
 *
 * <p>La cle est lue dans l'environnement par le fournisseur de secrets, jamais
 * ecrite dans le depot (section 17).
 */
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
