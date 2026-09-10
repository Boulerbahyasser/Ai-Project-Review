package ma.uae.aireviewer.configuration;

/** Parametres de generation de rapport. */
public record ReportConfig(
        String outputDirectory,
        String renderer,
        boolean compilePdf,
        String latexDockerImage) {
}
