package ma.uae.aireviewer.llm;

/**
 * Reponse vide, mal formee, ou non conforme au schema demande.
 * Cas rejouable une fois avec consigne de reformatage, puis abandon du critere.
 */
public final class LlmInvalidResponseException extends LlmException {

    public LlmInvalidResponseException(String message) {
        super(message);
    }

    public LlmInvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
