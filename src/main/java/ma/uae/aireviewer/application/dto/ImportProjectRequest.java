package ma.uae.aireviewer.application.dto;

import ma.uae.aireviewer.project.loader.ProjectSource;

/** Demande d'import venant de l'IHM. */
public record ImportProjectRequest(ProjectSource source) {
}
