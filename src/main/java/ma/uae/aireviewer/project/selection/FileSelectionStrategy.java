package ma.uae.aireviewer.project.selection;

import ma.uae.aireviewer.project.model.FileNode;

/**
 * Pattern Strategy : regle d'inclusion/exclusion des fichiers envoyes au LLM
 * (cahier des charges, section 3.1). Ajouter une strategie = ajouter une implementation.
 */
public interface FileSelectionStrategy {

    String name();

    boolean accepts(FileNode file);
}
