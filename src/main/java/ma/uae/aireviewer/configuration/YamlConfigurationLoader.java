package ma.uae.aireviewer.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/**
 * Charge {@code config/application.yaml}.
 *
 * <p>Lecture volontairement explicite, sans mapping automatique : chaque cle est
 * lue et validee nommement, de sorte qu'un fichier incomplet produise un message
 * designant la cle fautive plutot qu'un {@code NullPointerException} tardif.
 *
 * <p>Aucun secret n'est lu ici : la configuration ne contient que le nom des
 * variables d'environnement (section 17).
 */
public final class YamlConfigurationLoader implements ConfigurationLoader {

    @Override
    public AppConfiguration load(Path source) {
        Map<String, Object> root = readYaml(source);

        return new AppConfiguration(
                llm(section(root, "llm")),
                analysis(section(root, "analysis")),
                sandbox(section(root, "sandbox")),
                report(section(root, "report")),
                persistence(section(root, "persistence")));
    }

    // -------------------------------------------------------------- sections

    private LlmConfig llm(Map<String, Object> node) {
        return new LlmConfig(
                text(node, "llm.providerId"),
                text(node, "llm.model"),
                text(node, "llm.baseUrl"),
                text(node, "llm.apiKeyEnvVar"),
                integer(node, "llm.timeoutSeconds"),
                integer(node, "llm.maxRetries"),
                integer(node, "llm.maxOutputTokens"),
                decimal(node, "llm.temperature"),
                integer(node, "llm.maxCharsPerRequest"),
                integer(node, "llm.maxFilesPerRequest"),
                bool(node, "llm.cacheEnabled"),
                optionalText(node, "fallbackProviderId"));
    }

    private AnalysisConfig analysis(Map<String, Object> node) {
        return new AnalysisConfig(
                text(node, "analysis.criteriaProfile"),
                integer(node, "analysis.maxFileSizeKb"),
                strings(node, "analysis.includeGlobs"),
                strings(node, "analysis.excludeGlobs"));
    }

    private SandboxConfig sandbox(Map<String, Object> node) {
        return new SandboxConfig(
                bool(node, "sandbox.executionEnabled"),
                text(node, "sandbox.dockerImage"),
                decimal(node, "sandbox.cpuLimit"),
                text(node, "sandbox.memoryLimit"),
                integer(node, "sandbox.pidsLimit"),
                integer(node, "sandbox.timeoutSeconds"),
                bool(node, "sandbox.networkDisabled"),
                bool(node, "sandbox.readOnlyRootFs"),
                text(node, "sandbox.user"));
    }

    private ReportConfig report(Map<String, Object> node) {
        return new ReportConfig(
                text(node, "report.outputDirectory"),
                text(node, "report.renderer"),
                bool(node, "report.compilePdf"),
                text(node, "report.latexDockerImage"));
    }

    private PersistenceConfig persistence(Map<String, Object> node) {
        return new PersistenceConfig(
                text(node, "persistence.historyDirectory"),
                text(node, "persistence.cacheDirectory"));
    }

    // ------------------------------------------------------------ primitives

    private Map<String, Object> readYaml(Path source) {
        if (source == null || !Files.isReadable(source)) {
            throw new ConfigurationException("Fichier de configuration illisible : " + source);
        }
        try (InputStream in = Files.newInputStream(source)) {
            Object parsed = new Yaml().load(in);
            if (!(parsed instanceof Map<?, ?> map)) {
                throw new ConfigurationException(
                        "Le fichier " + source + " ne contient pas un objet YAML");
            }
            return cast(map);
        } catch (IOException unreadable) {
            throw new ConfigurationException("Lecture impossible de " + source, unreadable);
        }
    }

    private Map<String, Object> section(Map<String, Object> root, String name) {
        Object value = root.get(name);
        if (!(value instanceof Map<?, ?> map)) {
            throw new ConfigurationException("Section '" + name + "' absente de la configuration");
        }
        return cast(map);
    }

    private String text(Map<String, Object> node, String key) {
        Object value = require(node, key);
        String asText = String.valueOf(value).trim();
        if (asText.isEmpty()) {
            throw new ConfigurationException(key + " est vide");
        }
        return asText;
    }

    /** Cle facultative : rend {@code null} si absente ou explicitement nulle. */
    private String optionalText(Map<String, Object> node, String leaf) {
        Object value = node.get(leaf);
        if (value == null) {
            return null;
        }
        String asText = String.valueOf(value).trim();
        return asText.isEmpty() || "null".equals(asText) ? null : asText;
    }

    private int integer(Map<String, Object> node, String key) {
        Object value = require(node, key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException notANumber) {
            throw new ConfigurationException(key + " doit etre un entier, recu '" + value + "'");
        }
    }

    private double decimal(Map<String, Object> node, String key) {
        Object value = require(node, key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException notANumber) {
            throw new ConfigurationException(key + " doit etre un nombre, recu '" + value + "'");
        }
    }

    private boolean bool(Map<String, Object> node, String key) {
        Object value = require(node, key);
        if (value instanceof Boolean flag) {
            return flag;
        }
        String asText = String.valueOf(value).trim();
        if ("true".equalsIgnoreCase(asText) || "false".equalsIgnoreCase(asText)) {
            return Boolean.parseBoolean(asText);
        }
        throw new ConfigurationException(key + " doit valoir true ou false, recu '" + value + "'");
    }

    private List<String> strings(Map<String, Object> node, String key) {
        Object value = require(node, key);
        if (!(value instanceof List<?> list)) {
            throw new ConfigurationException(key + " doit etre une liste");
        }
        return list.stream().map(String::valueOf).map(String::trim).toList();
    }

    /** Le nom de cle passe est complet (« llm.model ») ; seule la feuille est cherchee. */
    private Object require(Map<String, Object> node, String key) {
        String leaf = key.substring(key.indexOf('.') + 1);
        Object value = node.get(leaf);
        if (value == null) {
            throw new ConfigurationException("Cle obligatoire absente : " + key);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> cast(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }
}
