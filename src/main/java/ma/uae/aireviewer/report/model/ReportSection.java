package ma.uae.aireviewer.report.model;

import java.util.List;

/** Section du rapport : un critere ou une partie de synthese. */
public record ReportSection(String title, List<String> paragraphs, List<String> bullets) {

    public ReportSection {
        paragraphs = List.copyOf(paragraphs);
        bullets = List.copyOf(bullets);
    }
}
