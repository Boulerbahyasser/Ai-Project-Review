package ma.uae.aireviewer.security.sandbox;

/** Execution refusee : politique de securite non respectee. */
public class SecurityPolicyViolationException extends RuntimeException {

    public SecurityPolicyViolationException(String message) {
        super(message);
    }
}
