package ma.uae.aireviewer.report.render;

import ma.uae.aireviewer.report.model.EvaluationReport;

/** Rendu HTML (extension optionnelle, section 18). */
public final class HtmlReportRenderer implements ReportRenderer {

    @Override
    public String format() {
        return "html";
    }

    @Override
    public String render(EvaluationReport report) {
        StringBuilder html = new StringBuilder("<!doctype html><html><head><meta charset=\"utf-8\"><title>")
                .append(escape(report.header().projectName())).append("</title></head><body><h1>")
                .append(escape(report.header().projectName())).append("</h1><p>")
                .append(escape(report.summary())).append("</p><table><tr><th>Critere</th><th>Score</th><th>Maximum</th></tr>");
        report.scoreTable().rows().forEach(row -> html.append("<tr><td>")
                .append(escape(row.criterion())).append("</td><td>").append(row.score())
                .append("</td><td>").append(row.maxScore()).append("</td></tr>"));
        html.append("</table>");
        report.sections().forEach(section -> {
            html.append("<section><h2>").append(escape(section.title())).append("</h2>");
            section.paragraphs().forEach(paragraph -> html.append("<p>").append(escape(paragraph)).append("</p>"));
            if (!section.bullets().isEmpty()) {
                html.append("<ul>");
                section.bullets().forEach(item -> html.append("<li>").append(escape(item)).append("</li>"));
                html.append("</ul>");
            }
            html.append("</section>");
        });
        return html.append("</body></html>").toString();
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
