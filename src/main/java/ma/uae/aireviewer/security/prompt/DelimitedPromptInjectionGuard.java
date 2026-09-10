package ma.uae.aireviewer.security.prompt;

/** Encadre le contenu par des delimiteurs et neutralise les delimiteurs internes. */
public final class DelimitedPromptInjectionGuard implements PromptInjectionGuard {

    @Override
    public String neutralize(UntrustedContent content) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean looksLikeInjection(UntrustedContent content) {
        throw new UnsupportedOperationException("TODO");
    }
}
