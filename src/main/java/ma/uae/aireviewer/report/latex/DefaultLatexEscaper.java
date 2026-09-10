package ma.uae.aireviewer.report.latex;

/** Echappement des caracteres speciaux LaTeX. */
public final class DefaultLatexEscaper implements LatexEscaper {

    @Override
    public String escape(String text) {
        throw new UnsupportedOperationException("TODO : echapper & % $ # _ { } ~ ^ et la barre oblique inverse");
    }
}
