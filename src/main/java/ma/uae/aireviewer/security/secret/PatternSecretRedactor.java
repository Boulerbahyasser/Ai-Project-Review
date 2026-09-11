package ma.uae.aireviewer.security.secret;

/** Masquage par motifs (cles d'API, jetons, en-tetes Authorization). */
public final class PatternSecretRedactor implements SecretRedactor {

    @Override
    public String redact(String text) {
        if (text == null || text.isEmpty()) return text;
        String redacted = text;
        redacted = redacted.replaceAll("(?i)(authorization\\s*:\\s*bearer\\s+)[^\\s,;]+", "$1[REDACTED]");
        redacted = redacted.replaceAll("(?i)((?:api[-_ ]?key|token|password|secret)\\s*[:=]\\s*)[^\\s,;]+",
                "$1[REDACTED]");
        redacted = redacted.replaceAll("(?i)\\b(sk-[A-Za-z0-9_-]{12,}|gh[pousr]_[A-Za-z0-9_]{12,})\\b",
                "[REDACTED]");
        return redacted;
    }
}
