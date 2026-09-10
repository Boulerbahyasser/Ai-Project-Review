package ma.uae.aireviewer.llm;

/** Delai d'attente depasse. Transitoire : le serveur peut repondre au prochain essai. */
public final class LlmTimeoutException extends LlmException {

    public LlmTimeoutException(String message) {
        super(message);
    }

    public LlmTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
