package ma.uae.aireviewer.configuration;

import java.util.Optional;

/**
 * Acces aux secrets (cles d'API). Aucune cle ne doit figurer dans le depot
 * (cahier des charges, section 17).
 */
public interface SecretProvider {

    Optional<String> secret(String name);
}
