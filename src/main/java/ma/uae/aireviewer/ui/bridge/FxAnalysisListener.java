package ma.uae.aireviewer.ui.bridge;

import javafx.application.Platform;
import ma.uae.aireviewer.application.event.AnalysisEvent;
import ma.uae.aireviewer.application.event.AnalysisListener;

/** Rebascule les evenements du moteur vers le thread JavaFX. */
public final class FxAnalysisListener implements AnalysisListener {

    private final AnalysisListener delegate;

    public FxAnalysisListener(AnalysisListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onEvent(AnalysisEvent event) {
        Platform.runLater(() -> delegate.onEvent(event));
    }
}
