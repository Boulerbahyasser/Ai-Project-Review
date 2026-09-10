package ma.uae.aireviewer.project.model;

import java.nio.file.Path;
import java.time.Instant;

/** Identification du projet analyse, reprise dans le rapport. */
public record ProjectMetadata(String name, Path location, ProjectOrigin origin, Instant importedAt) {
}
