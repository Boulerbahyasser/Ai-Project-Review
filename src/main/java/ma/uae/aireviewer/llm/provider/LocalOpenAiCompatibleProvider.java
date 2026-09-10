package ma.uae.aireviewer.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ma.uae.aireviewer.configuration.LlmConfig;
import ma.uae.aireviewer.llm.LlmException;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.TokenUsage;

/**
 * Modele execute localement et expose via une API compatible OpenAI.
 *
 * <p>Couvre LM Studio ({@code http://localhost:1234/v1}) et l'interface compatible
 * d'Ollama ({@code http://localhost:11434/v1}). Utiliser ce point d'entree plutot
 * que l'API native d'Ollama permet a un seul adaptateur de servir les deux outils.
 *
 * <p>Fournisseur par defaut : aucune cle requise, et aucun code du projet analyse
 * ne quitte la machine.
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
