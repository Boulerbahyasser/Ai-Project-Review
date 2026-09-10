package ma.uae.aireviewer.configuration;

/** Parametres d'isolation Docker. */
public record SandboxConfig(
        boolean executionEnabled,
        String dockerImage,
        double cpuLimit,
        String memoryLimit,
        int pidsLimit,
        int timeoutSeconds,
        boolean networkDisabled,
        boolean readOnlyRootFs,
        String user) {
}
