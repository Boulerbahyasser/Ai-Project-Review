package ma.uae.aireviewer.application.dto;

/** Demande de generation de rapport. */
public record GenerateReportRequest(String analysisId, String format, boolean compilePdf) {
}
