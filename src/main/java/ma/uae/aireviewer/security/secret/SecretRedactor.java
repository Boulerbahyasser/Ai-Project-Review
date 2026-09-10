package ma.uae.aireviewer.security.secret;

/** Masque cles, jetons et mots de passe avant journalisation (section 12). */
public interface SecretRedactor {

    String redact(String text);
}
