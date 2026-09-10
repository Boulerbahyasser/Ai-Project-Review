package ma.uae.aireviewer.security.sandbox;

/** Implementation par defaut : refuse toute execution. */
public final class DisabledSandboxRunner implements SandboxRunner {

    @Override
    public SandboxResult run(SandboxSpec spec) {
        throw new SecurityPolicyViolationException("Execution du projet analyse desactivee");
    }
}
