package ma.uae.aireviewer.project.classification;

import java.nio.file.Path;
import ma.uae.aireviewer.project.model.FileType;

/** Regle unitaire de classification : ajouter un type = ajouter une regle. */
public interface ClassificationRule {

    boolean matches(Path path);

    FileType type();
}
