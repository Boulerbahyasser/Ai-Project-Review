package ma.uae.aireviewer.security.policy;

import ma.uae.aireviewer.security.sandbox.SandboxSpec;
import ma.uae.aireviewer.security.sandbox.SecurityPolicyViolationException;

/** Refuse root, le reseau actif, l'absence de limites ou de delai maximal. */
public final class LeastPrivilegePolicy implements ExecutionPolicy {

    @Override
    public void verify(SandboxSpec spec) {
        if (spec == null || spec.image() == null || spec.image().isBlank()
                || spec.projectDirectory() == null || !java.nio.file.Files.isDirectory(spec.projectDirectory())
                || spec.command().isEmpty() || spec.limits() == null) {
            throw new IllegalArgumentException("Specification de sandbox incomplete");
        }
        var limits = spec.limits();
        if (limits.cpus() <= 0 || limits.memory() == null || limits.memory().isBlank()
                || limits.pids() <= 0 || limits.timeout() == null || limits.timeout().isZero()
                || limits.timeout().isNegative()) {
            throw new SecurityPolicyViolationException("Limites de sandbox invalides");
        }
        if (!limits.networkDisabled() || !limits.readOnlyRootFs()
                || limits.user() == null || limits.user().isBlank()
                || "root".equalsIgnoreCase(limits.user()) || "0".equals(limits.user())) {
            throw new SecurityPolicyViolationException("La sandbox ne respecte pas le moindre privilege");
        }
    }
}
