package ma.uae.aireviewer.report.latex;

/** Echappe le texte issu du projet analyse ou du LLM avant insertion LaTeX. */
public interface LatexEscaper {

    String escape(String text);
}
