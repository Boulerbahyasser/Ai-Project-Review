package ma.uae.aireviewer.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ma.uae.aireviewer.llm.LlmInvalidResponseException;
import ma.uae.aireviewer.llm.LlmRequest;
import ma.uae.aireviewer.llm.TokenUsage;

/**
 * Traduction pour les API de type « chat completions » compatibles OpenAI.
 *
 * <p>Mistral, DeepSeek, LM Studio et l'interface compatible d'Ollama partagent ce
 * format. Le mutualiser ici evite de reecrire trois fois la meme traduction :
 * chaque fournisseur ne conserve alors que son URL et son mode d'authentification.
 *
 * <p>Classe utilitaire volontairement sans etat : elle ne fait que convertir, dans
 * les deux sens, entre nos types et le format du fournisseur.
 */
final class OpenAiChatFormat {

    private OpenAiChatFormat() {
    }

    /**
     * Construit le corps de la requete.
     *
     * <p>Le corps est assemble comme un arbre Jackson, jamais par concatenation de
     * chaines : les prompts contiennent du code source, donc des guillemets, des
     * antislashs et des retours a la ligne. Un assemblage manuel produirait du JSON
     * invalide des le premier fichier Java analyse.
     */
    static ObjectNode requestBody(LlmRequest request, ObjectNode body) {
        body.put("model", request.model());

        ArrayNode messages = body.putArray("messages");
        if (request.systemPrompt() != null && !request.systemPrompt().isBlank()) {
            ObjectNode system = messages.addObject();
            system.put("role", "system");
            system.put("content", request.systemPrompt());
        }
        ObjectNode user = messages.addObject();
        user.put("role", "user");
        user.put("content", request.userPrompt());

        body.put("temperature", request.temperature());
        body.put("max_tokens", request.maxOutputTokens());
        body.put("stream", false);

        if (request.jsonMode()) {
            body.putObject("response_format").put("type", "json_object");
        }
        return body;
    }

    /** Extrait le texte utile : {@code choices[0].message.content}. */
    static String content(JsonNode response) throws LlmInvalidResponseException {
        JsonNode choices = response.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            throw new LlmInvalidResponseException(
                    "Reponse sans tableau 'choices' exploitable : " + summarize(response));
        }
        JsonNode content = choices.path(0).path("message").path("content");
        if (content.isMissingNode() || !content.isTextual()) {
            throw new LlmInvalidResponseException(
                    "Champ choices[0].message.content absent : " + summarize(response));
        }
        return content.asText();
    }

    /** Jetons consommes, quand le fournisseur les communique. */
    static TokenUsage usage(JsonNode response) {
        JsonNode usage = response.path("usage");
        if (usage.isMissingNode()) {
            return TokenUsage.UNKNOWN;
        }
        return new TokenUsage(
                usage.path("prompt_tokens").asInt(0),
                usage.path("completion_tokens").asInt(0));
    }

    /** Nom du modele reellement servi : il peut differer de celui demande. */
    static String servedModel(JsonNode response, String requested) {
        String served = response.path("model").asText("");
        return served.isBlank() ? requested : served;
    }

    private static String summarize(JsonNode response) {
        String text = response.toString();
        return text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }
}
