package ma.uae.aireviewer.analysis.criterion;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import org.yaml.snakeyaml.Yaml;

/** Catalogue lisant les profils YAML du repertoire config/profiles. */
public final class YamlCriterionCatalog implements CriterionCatalog {

    private final Path profilesDirectory;

    public YamlCriterionCatalog(Path profilesDirectory) {
        this.profilesDirectory = profilesDirectory;
    }

    @Override
    public List<String> profileNames() {
        if (!Files.isDirectory(profilesDirectory)) {
            return List.of();
        }
        try (var paths = Files.list(profilesDirectory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".yaml") || name.endsWith(".yml");
                    })
                    .map(path -> {
                        String name = path.getFileName().toString();
                        int dot = name.lastIndexOf('.');
                        return name.substring(0, dot);
                    })
                    .sorted()
                    .toList();
        } catch (IOException failure) {
            throw new IllegalStateException("Lecture impossible de " + profilesDirectory, failure);
        }
    }

    @Override
    public CriterionProfile profile(String name) {
        if (name == null || name.isBlank() || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Nom de profil invalide : " + name);
        }
        Path file = profilesDirectory.resolve(name + ".yaml");
        if (!Files.isRegularFile(file)) {
            file = profilesDirectory.resolve(name + ".yml");
        }
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Profil introuvable : " + name);
        }
        try (var input = Files.newInputStream(file)) {
            Object value = new Yaml().load(input);
            if (!(value instanceof Map<?, ?> root)) {
                throw new IllegalArgumentException("Profil YAML invalide : " + name);
            }
            String profileName = text(root, "name", name);
            Object criteriaNode = root.get("criteria");
            if (!(criteriaNode instanceof List<?> criteria)) {
                throw new IllegalArgumentException("Le profil doit contenir une liste criteria");
            }
            List<Criterion> parsed = new ArrayList<>();
            for (Object item : criteria) {
                if (!(item instanceof Map<?, ?> criterion)) {
                    throw new IllegalArgumentException("Critere YAML invalide dans " + name);
                }
                String id = text(criterion, "id", null);
                String label = text(criterion, "label", id);
                int max = integer(criterion, "maxScore", 10);
                double weight = decimal(criterion, "weight", 1.0);
                AnalysisKind kind;
                try {
                    kind = AnalysisKind.valueOf(text(criterion, "kind", "LLM").toUpperCase());
                } catch (IllegalArgumentException invalidKind) {
                    throw new IllegalArgumentException("kind invalide pour " + id, invalidKind);
                }
                if (max <= 0 || weight < 0) {
                    throw new IllegalArgumentException("Score ou poids invalide pour " + id);
                }
                parsed.add(new Criterion(new CriterionId(id), label,
                        text(criterion, "description", ""), max, weight, kind));
            }
            return new CriterionProfile(profileName, parsed);
        } catch (IOException failure) {
            throw new IllegalStateException("Lecture impossible du profil " + name, failure);
        }
    }

    private static String text(Map<?, ?> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value).trim();
    }

    private static int integer(Map<?, ?> map, String key, int fallback) {
        Object value = map.get(key);
        if (value == null) return fallback;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private static double decimal(Map<?, ?> map, String key, double fallback) {
        Object value = map.get(key);
        if (value == null) return fallback;
        if (value instanceof Number number) return number.doubleValue();
        return Double.parseDouble(String.valueOf(value));
    }
}
