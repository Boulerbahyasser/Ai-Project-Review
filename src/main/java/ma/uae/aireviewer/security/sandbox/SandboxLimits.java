package ma.uae.aireviewer.security.sandbox;

import java.time.Duration;

/** Limites appliquees au conteneur (moindre privilege, section 8.1). */
public record SandboxLimits(
        double cpus,
        String memory,
        int pids,
        Duration timeout,
        boolean networkDisabled,
        boolean readOnlyRootFs,
        String user) {
}
