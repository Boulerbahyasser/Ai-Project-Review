package ma.uae.aireviewer.analysis;

import ma.uae.aireviewer.analysis.result.AnalysisResult;

/**
 * Moteur d'evaluation (cahier des charges, section 3.3).
 * Interface : l'IHM et les cas d'utilisation ne dependent jamais d'une implementation.
 */
public interface AnalysisEngine {

    AnalysisResult run(AnalysisContext context);
}
