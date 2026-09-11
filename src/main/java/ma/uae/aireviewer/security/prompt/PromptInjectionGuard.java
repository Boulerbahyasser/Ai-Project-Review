package ma.uae.aireviewer.security.prompt;

/**
 * Defense contre l'injection de prompt (section 8.2) : le code analyse est une
 * donnee, jamais une instruction.
 */
public interface PromptInjectionGuard {

    String neutralize(UntrustedContent content);

    boolean looksLikeInjection(UntrustedContent content);
}
