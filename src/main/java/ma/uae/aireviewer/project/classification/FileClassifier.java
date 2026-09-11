package ma.uae.aireviewer.project.classification;

import java.nio.file.Path;
import ma.uae.aireviewer.project.model.FileType;

/** Determine le type d'un fichier du projet importe. */
public interface FileClassifier {

    FileType classify(Path path);
}
