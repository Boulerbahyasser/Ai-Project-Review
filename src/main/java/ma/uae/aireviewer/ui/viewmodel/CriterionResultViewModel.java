package ma.uae.aireviewer.ui.viewmodel;

import java.util.List;

/** Etat d'affichage d'un resultat de critere. */
public record CriterionResultViewModel(
        String label,
        int score,
        int maxScore,
        String status,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations) {
}
