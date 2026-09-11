package ma.uae.aireviewer.ui.service;

import java.nio.file.Path;
import java.util.List;

/**
 * Projet charge, pret a etre evalue.
 *
 * <p>Porte les deux representations dont l'interface a besoin : le chemin racine
 * pour afficher l'arborescence, et le code source concatene que
 * {@code ResilientEvaluator} attend sous forme de chaine.
 */
public record ProjectSnapshot(String name, Path root, List<Path> sourceFiles, String sourceCode) {

    public ProjectSnapshot {
        sourceFiles = List.copyOf(sourceFiles);
    }

    public int fileCount() {
        return sourceFiles.size();
    }
}
