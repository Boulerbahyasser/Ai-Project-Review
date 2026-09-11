package ma.uae.aireviewer;

import ma.uae.aireviewer.ui.AiReviewerApplication;

/** Lanceur : classe principale du jar, delegue a JavaFX. */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        javafx.application.Application.launch(AiReviewerApplication.class, args);
    }
}
