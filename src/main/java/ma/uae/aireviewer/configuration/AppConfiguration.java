package ma.uae.aireviewer.configuration;

/** Configuration complete de l'application, immuable, injectee au demarrage. */
public record AppConfiguration(
        LlmConfig llm,
        AnalysisConfig analysis,
        SandboxConfig sandbox,
        ReportConfig report,
        PersistenceConfig persistence) {
}
