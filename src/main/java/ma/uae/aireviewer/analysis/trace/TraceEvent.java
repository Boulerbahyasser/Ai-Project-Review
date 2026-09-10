package ma.uae.aireviewer.analysis.trace;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Entree de tracabilite (cahier des charges, section 12) : debut/fin, critere execute,
 * modele utilise, duree, nombre d'appels, erreurs.
 * Les valeurs sont passees par SecretRedactor avant enregistrement.
 */
public record TraceEvent(
        Instant timestamp,
        String category,
        String message,
        Duration duration,
        Map<String, String> attributes) {

    public TraceEvent {
        attributes = Map.copyOf(attributes);
    }

    public static TraceEvent of(String category, String message) {
        return new TraceEvent(Instant.now(), category, message, Duration.ZERO, Map.of());
    }
}
