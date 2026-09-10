package ma.uae.aireviewer.application.event;

/** Abonne aux evenements d'analyse. L'IHM en fournit une implementation. */
public interface AnalysisListener {

    void onEvent(AnalysisEvent event);
}
