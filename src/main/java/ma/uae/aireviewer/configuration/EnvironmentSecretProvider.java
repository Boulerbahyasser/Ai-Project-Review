package ma.uae.aireviewer.configuration;

import java.util.Optional;

/** Lit les secrets dans les variables d'environnement du processus. */
public final class EnvironmentSecretProvider implements SecretProvider {

    @Override
    public Optional<String> secret(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(System.getenv(name)).filter(v -> !v.isBlank());
    }
}
