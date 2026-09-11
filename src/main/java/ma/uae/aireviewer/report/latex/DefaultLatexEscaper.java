package ma.uae.aireviewer.report.latex;

/** Echappement des caracteres speciaux LaTeX. */
public final class DefaultLatexEscaper implements LatexEscaper {

    @Override
    public String escape(String text) {
        if (text == null) return "";
        StringBuilder escaped = new StringBuilder(text.length() + 16);
        for (char character : text.toCharArray()) {
            switch (character) {
                case '\\' -> escaped.append("\\textbackslash{}");
                case '&' -> escaped.append("\\&");
                case '%' -> escaped.append("\\%");
                case '$' -> escaped.append("\\$");
                case '#' -> escaped.append("\\#");
                case '_' -> escaped.append("\\_");
                case '{' -> escaped.append("\\{");
                case '}' -> escaped.append("\\}");
                case '~' -> escaped.append("\\textasciitilde{}");
                case '^' -> escaped.append("\\textasciicircum{}");
                default -> escaped.append(character);
            }
        }
        return escaped.toString();
    }
}
