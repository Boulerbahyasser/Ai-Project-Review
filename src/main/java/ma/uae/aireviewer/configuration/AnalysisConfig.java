package ma.uae.aireviewer.configuration;

import java.util.List;

/**
 * Parametres du moteur d'analyse et de la selection de fichiers.
 *
 * <p>Les limites de contexte ne figurent pas ici : elles decrivent le modele et
 * vivent dans {@link LlmConfig}.
 */
public record AnalysisConfig(
        String criteriaProfile,
        int maxFileSizeKb,
        List<String> includeGlobs,
        List<String> excludeGlobs) {

    public AnalysisConfig {
        includeGlobs = List.copyOf(includeGlobs);
        excludeGlobs = List.copyOf(excludeGlobs);
        if (maxFileSizeKb <= 0) {
            throw new ConfigurationException(
                    "analysis.maxFileSizeKb doit etre strictement positif, recu " + maxFileSizeKb);
        }
        if (criteriaProfile == null || criteriaProfile.isBlank()) {
            throw new ConfigurationException("analysis.criteriaProfile est obligatoire");
        }
    }
}
