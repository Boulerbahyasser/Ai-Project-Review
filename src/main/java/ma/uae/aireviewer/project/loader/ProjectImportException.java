package ma.uae.aireviewer.project.loader;

/** Echec d'import : source introuvable, archive corrompue, clone impossible. */
public class ProjectImportException extends RuntimeException {

    public ProjectImportException(String message) {
        super(message);
    }

    public ProjectImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
