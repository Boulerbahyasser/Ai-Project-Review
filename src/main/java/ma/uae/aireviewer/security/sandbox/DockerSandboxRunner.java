package ma.uae.aireviewer.security.sandbox;

import ma.uae.aireviewer.security.policy.ExecutionPolicy;

/** Execution dans un conteneur Docker dedie, supprime apres usage. */
public final class DockerSandboxRunner implements SandboxRunner {

    private final ExecutionPolicy policy;

    public DockerSandboxRunner(ExecutionPolicy policy) {
        this.policy = policy;
    }

    @Override
    public SandboxResult run(SandboxSpec spec) {
        throw new UnsupportedOperationException("TODO : docker run --rm --network none --read-only ...");
    }
}
