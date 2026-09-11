package ma.uae.aireviewer.configuration;

/** Parametres d'isolation Docker pour la compilation LaTeX (section 8). */
public record LatexConfig(
        String dockerImage,
        String memoryLimit,
        int pidsLimit,
        int timeoutSeconds,
        String user) {
}
