package ma.uae.aireviewer.report.latex;

import java.util.Map;

/** Gabarit LaTeX charge depuis les ressources. */
public record LatexTemplate(String content) {

    public String render(Map<String, String> variables) {
        String rendered = content == null ? "" : content;
        if (variables == null) return rendered;
        for (var entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() == null ? "" : entry.getValue());
        }
        return rendered;
    }
}
