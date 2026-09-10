package ma.uae.aireviewer.security.sandbox;

/** Resultat d'une execution isolee. */
public record SandboxResult(int exitCode, String stdout, String stderr, boolean timedOut) {
}
