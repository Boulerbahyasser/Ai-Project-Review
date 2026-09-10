package ma.uae.aireviewer.analysis.criterion;

import java.util.List;

/** Acces aux profils de criteres disponibles (config/profiles). */
public interface CriterionCatalog {

    List<String> profileNames();

    CriterionProfile profile(String name);
}
