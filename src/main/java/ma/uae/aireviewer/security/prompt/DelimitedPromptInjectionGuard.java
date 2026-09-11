package ma.uae.aireviewer.security.prompt;

/** Encadre le contenu par des delimiteurs et neutralise les delimiteurs internes. */
public final class DelimitedPromptInjectionGuard implements PromptInjectionGuard {

    @Override
    public String neutralize(UntrustedContent content) {
        String value = content == null || content.value() == null ? "" : content.value();
        String safe = value.replace("[[UNTRUSTED_CONTENT]]", "[[UNTRUSTED_CONTENT_ESCAPED]]")
                .replace("[[/UNTRUSTED_CONTENT]]", "[[/UNTRUSTED_CONTENT_ESCAPED]]");
        return "[[UNTRUSTED_CONTENT]]\n" + safe + "\n[[/UNTRUSTED_CONTENT]]";
    }

    @Override
    public boolean looksLikeInjection(UntrustedContent content) {
        if (content == null || content.value() == null) return false;
        String value = content.value().toLowerCase(java.util.Locale.ROOT);
        return value.contains("ignore previous instructions")
                || value.contains("system prompt")
                || value.contains("jailbreak")
                || value.contains("you are now");
    }
}
