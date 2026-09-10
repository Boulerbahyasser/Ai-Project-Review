package ma.uae.aireviewer.analysis.criterion;

import java.nio.file.Path;
import java.util.List;

/** Catalogue lisant les profils YAML du repertoire config/profiles. */
public final class YamlCriterionCatalog implements CriterionCatalog {

    private final Path profilesDirectory;

    public YamlCriterionCatalog(Path profilesDirectory) {
        this.profilesDirectory = profilesDirectory;
    }

    @Override
    public List<String> profileNames() {
        throw new UnsupportedOperationException("TODO : lister les fichiers *.yaml du repertoire");
    }

    @Override
    public CriterionProfile profile(String name) {
        throw new UnsupportedOperationException("TODO : parser le profil et valider les criteres");
    }
}
