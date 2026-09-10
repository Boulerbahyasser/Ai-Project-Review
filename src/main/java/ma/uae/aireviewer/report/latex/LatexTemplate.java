package ma.uae.aireviewer.report.latex;

import java.util.Map;

/** Gabarit LaTeX charge depuis les ressources. */
public record LatexTemplate(String content) {

    public String render(Map<String, String> variables) {
        throw new UnsupportedOperationException("TODO : substituer {{cle}}");
    }
}
