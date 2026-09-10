package ma.uae.aireviewer.configuration;

import java.nio.file.Path;

/** Chargement de config/application.yaml via SnakeYAML. */
public final class YamlConfigurationLoader implements ConfigurationLoader {

    @Override
    public AppConfiguration load(Path source) {
        throw new UnsupportedOperationException("TODO : parser le YAML et valider les valeurs");
    }
}
