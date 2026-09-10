package ma.uae.aireviewer.llm.resilience;

import java.time.Duration;

/** Politique de reprise : nombre maximal de tentatives et attente exponentielle. */
public record RetryPolicy(int maxAttempts, Duration initialBackoff, double multiplier) {

    public RetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts doit valoir au moins 1");
        }
    }

    public static RetryPolicy defaults(int maxAttempts) {
        return new RetryPolicy(Math.max(1, maxAttempts), Duration.ofMillis(500), 2.0);
    }

    public Duration backoffFor(int attempt) {
        return Duration.ofMillis((long) (initialBackoff.toMillis() * Math.pow(multiplier, attempt - 1)));
    }
}
