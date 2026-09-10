package ma.uae.aireviewer.llm;

/** Service indisponible (serveur local arrete, erreur HTTP 5xx) : cas rejouable. */
public final class LlmUnavailableException extends LlmException {

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
