package ma.uae.aireviewer.security.policy;

import ma.uae.aireviewer.security.sandbox.SandboxSpec;

/** Verifie la demande d'execution avant lancement. */
public interface ExecutionPolicy {

    void verify(SandboxSpec spec);
}
