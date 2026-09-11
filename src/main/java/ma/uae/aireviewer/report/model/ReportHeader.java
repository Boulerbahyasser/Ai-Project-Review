package ma.uae.aireviewer.report.model;

import java.time.Instant;

/** Identification du rapport (section 7). */
public record ReportHeader(
        String projectName,
        Instant analysisDate,
        String configurationSummary,
        String profileName,
        String modelUsed) {
}
