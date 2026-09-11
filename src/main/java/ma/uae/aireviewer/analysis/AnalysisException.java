package ma.uae.aireviewer.analysis;

/** Echec non recuperable du moteur d'analyse (un echec de critere donne un CriterionResult FAILED). */
public class AnalysisException extends RuntimeException {

    public AnalysisException(String message) {
        super(message);
    }

    public AnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
