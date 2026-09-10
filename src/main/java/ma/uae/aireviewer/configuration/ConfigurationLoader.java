package ma.uae.aireviewer.configuration;

import java.nio.file.Path;

/** Charge la configuration depuis une source externe (YAML par defaut). */
public interface ConfigurationLoader {

    AppConfiguration load(Path source);
}
