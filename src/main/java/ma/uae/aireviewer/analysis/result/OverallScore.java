package ma.uae.aireviewer.analysis.result;

/** Score global consolide. */
public record OverallScore(double score, double maxScore) {

    public double percentage() {
        return maxScore == 0 ? 0 : (score / maxScore) * 100;
    }
}
