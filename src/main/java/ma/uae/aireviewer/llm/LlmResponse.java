package ma.uae.aireviewer.llm;

import java.time.Duration;

/** Reponse brute d'un modele. Le contenu n'est jamais utilise sans validation prealable. */
public record LlmResponse(
        String content,
        String model,
        int promptTokens,
        int completionTokens,
        Duration latency) {

    public boolean isEmpty() {
        return content == null || content.isBlank();
    }
}
