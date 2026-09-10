package ma.uae.aireviewer.security.policy;

import ma.uae.aireviewer.security.sandbox.SandboxSpec;

/** Refuse root, le reseau actif, l'absence de limites ou de delai maximal. */
public final class LeastPrivilegePolicy implements ExecutionPolicy {

    @Override
    public void verify(SandboxSpec spec) {
        throw new UnsupportedOperationException("TODO");
    }
}
