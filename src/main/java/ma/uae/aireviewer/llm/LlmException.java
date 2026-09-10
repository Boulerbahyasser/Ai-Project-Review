package ma.uae.aireviewer.llm;

/**
 * Echec d'un appel au modele. Exception verifiee : les appelants sont contraints
 * de traiter le cas d'erreur (cahier des charges, section 5).
 */
public class LlmException extends Exception {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
