package ma.uae.aireviewer.report.assembly;

import ma.uae.aireviewer.analysis.result.AnalysisResult;
import ma.uae.aireviewer.report.model.EvaluationReport;

/** Transforme un resultat d'analyse en rapport. La structure ne depend pas du LLM. */
public interface ReportAssembler {

    EvaluationReport assemble(AnalysisResult result);
}
