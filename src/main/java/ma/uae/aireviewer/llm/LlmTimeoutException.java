package ma.uae.aireviewer.llm;

/** Delai d'attente depasse : cas rejouable. */
public final class LlmTimeoutException extends LlmException {

    public LlmTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
