package ma.uae.aireviewer.llm;

/**
 * Echec d'un appel au modele. Exception verifiee : les appelants sont contraints
 * de traiter le cas d'erreur (cahier des charges, section 5).
 *
 * <p>{@link #isRetryable()} porte la decision de rejouabilite. Les decorateurs de
 * resilience s'appuient sur ce drapeau, jamais sur une chaine de {@code instanceof} :
 * ajouter un type d'erreur n'impose donc pas de modifier la politique de reprise.
 */
public class LlmException extends Exception {

    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Indique si relancer la meme requete a une chance d'aboutir.
     * Faux par defaut : une erreur est consideree definitive tant qu'elle n'est
     * pas explicitement declaree transitoire.
     */
    public boolean isRetryable() {
        return false;
    }
}
