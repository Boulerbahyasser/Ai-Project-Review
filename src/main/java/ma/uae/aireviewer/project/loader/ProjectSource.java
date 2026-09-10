package ma.uae.aireviewer.project.loader;

import java.nio.file.Path;

/** Source d'import demandee par l'utilisateur (cahier des charges, section 3.1). */
public sealed interface ProjectSource {

    record LocalDirectory(Path directory) implements ProjectSource {}

    record Archive(Path archive) implements ProjectSource {}

    record GitRepository(String url, String reference) implements ProjectSource {}
}
