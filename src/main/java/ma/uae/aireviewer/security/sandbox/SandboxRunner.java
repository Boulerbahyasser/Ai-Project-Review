package ma.uae.aireviewer.security.sandbox;

/** Execution de code non fiable. Jamais sur la machine hote. */
public interface SandboxRunner {

    SandboxResult run(SandboxSpec spec);
}
