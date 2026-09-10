package ma.uae.aireviewer.ui.viewmodel;

/** Etat d'affichage de la progression. */
public record AnalysisProgressViewModel(int done, int total, String currentCriterion) {

    public double ratio() {
        return total == 0 ? 0 : (double) done / total;
    }
}
