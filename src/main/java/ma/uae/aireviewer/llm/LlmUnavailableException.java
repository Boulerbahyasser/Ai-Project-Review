package ma.uae.aireviewer.llm;

/**
 * Service injoignable : serveur local arrete, erreur HTTP 5xx, quota momentanement
 * depasse. Transitoire.
 */
public final class LlmUnavailableException extends LlmException {

    public LlmUnavailableException(String message) {
        super(message);
    }

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
