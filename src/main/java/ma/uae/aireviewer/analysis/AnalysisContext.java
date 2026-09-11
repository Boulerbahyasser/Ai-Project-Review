package ma.uae.aireviewer.analysis;

import java.util.List;
import ma.uae.aireviewer.analysis.criterion.CriterionProfile;
import ma.uae.aireviewer.analysis.trace.TraceRecorder;
import ma.uae.aireviewer.project.model.FileNode;
import ma.uae.aireviewer.project.model.SoftwareProject;

/**
 * Donnees d'entree d'une analyse, transmises a chaque Analyzer.
 * selectedFiles resulte deja de la strategie de selection : un Analyzer ne
 * reparcourt jamais le projet complet.
 */
public record AnalysisContext(
        String analysisId,
        SoftwareProject project,
        List<FileNode> selectedFiles,
        CriterionProfile profile,
        TraceRecorder trace) {

    public AnalysisContext {
        selectedFiles = List.copyOf(selectedFiles);
    }
}
