package ma.uae.aireviewer.security.sandbox;

import java.nio.file.Path;
import java.util.List;

/** Demande d'execution isolee : projet monte en lecture seule + commande. */
public record SandboxSpec(String image, Path projectDirectory, List<String> command, SandboxLimits limits) {

    public SandboxSpec {
        command = List.copyOf(command);
    }
}
