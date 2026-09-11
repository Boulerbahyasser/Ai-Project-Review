package ma.uae.aireviewer.project.loader;

import ma.uae.aireviewer.project.model.SoftwareProject;

/**
 * Charge un projet depuis une source. Ajouter un mode d'import = ajouter une
 * implementation et l'enregistrer dans ProjectLoaderFactory.
 */
public interface ProjectLoader {

    boolean supports(ProjectSource source);

    SoftwareProject load(ProjectSource source);
}
