package ma.uae.aireviewer.configuration;

import java.util.List;

/** Parametres du moteur d'analyse et de la selection de fichiers. */
public record AnalysisConfig(
        String criteriaProfile,
        int maxFileSizeKb,
        int maxFilesPerRequest,
        int maxCharsPerRequest,
        List<String> includeGlobs,
        List<String> excludeGlobs) {
}
