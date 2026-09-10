package ma.uae.aireviewer.ui.controller;

import ma.uae.aireviewer.application.AiReviewerFacade;

/** Traduit les actions utilisateur en appels de la facade. Aucun calcul metier. */
public final class MainController {

    private final AiReviewerFacade facade;

    public MainController(AiReviewerFacade facade) {
        this.facade = facade;
    }

    public void onSelectProject() {
        throw new UnsupportedOperationException("TODO");
    }

    public void onStartAnalysis() {
        throw new UnsupportedOperationException("TODO");
    }

    public void onGenerateReport() {
        throw new UnsupportedOperationException("TODO");
    }
}
