package ma.uae.aireviewer.llm;

/**
 * Reponse vide, mal formee, ou non conforme au schema demande.
 *
 * <p>Rejouable une seule fois, avec une consigne de reformatage : c'est au
 * decorateur de reprise d'appliquer cette limite, le drapeau se contentant
 * d'autoriser une nouvelle tentative.
 */
public final class LlmInvalidResponseException extends LlmException {

    public LlmInvalidResponseException(String message) {
        super(message);
    }

    public LlmInvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public boolean isRetryable() {
        return true;
    }
}
