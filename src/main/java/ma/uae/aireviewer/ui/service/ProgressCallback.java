package ma.uae.aireviewer.ui.service;

/**
 * Rapport d'avancement fourni par l'interface sous forme de lambda.
 *
 * <p>Le sous-systeme LLM n'expose pas de rappel de progression ; c'est la couche
 * de liaison qui en produit un, en evaluant les criteres un par un.
 */
@FunctionalInterface
public interface ProgressCallback {

    void report(int completed, int total, String message);

    static ProgressCallback none() {
        return (completed, total, message) -> { };
    }
}
