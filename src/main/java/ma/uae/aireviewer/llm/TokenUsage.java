package ma.uae.aireviewer.llm;

/**
 * Jetons consommes par un appel. Renseigne quand le fournisseur les communique,
 * {@link #UNKNOWN} sinon : certains serveurs locaux ne les renvoient pas.
 *
 * <p>Alimente la trace (section 12) et l'estimation de cout citee parmi les
 * extensions optionnelles (section 18).
 */
public record TokenUsage(int promptTokens, int completionTokens) {

    public static final TokenUsage UNKNOWN = new TokenUsage(0, 0);

    public int total() {
        return promptTokens + completionTokens;
    }
}
