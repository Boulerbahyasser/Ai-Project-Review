package ma.uae.aireviewer.report.render;

import java.util.List;

/** Selectionne le rendu selon la configuration. */
public final class ReportRendererRegistry {

    private final List<ReportRenderer> renderers;

    public ReportRendererRegistry(List<ReportRenderer> renderers) {
        this.renderers = List.copyOf(renderers);
    }

    public ReportRenderer byFormat(String format) {
        return renderers.stream()
                .filter(renderer -> renderer.format().equals(format))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Format inconnu : " + format));
    }
}
