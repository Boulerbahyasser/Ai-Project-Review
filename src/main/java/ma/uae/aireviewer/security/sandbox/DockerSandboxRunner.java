package ma.uae.aireviewer.security.sandbox;

import ma.uae.aireviewer.security.policy.ExecutionPolicy;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/** Execution dans un conteneur Docker dedie, supprime apres usage. */
public final class DockerSandboxRunner implements SandboxRunner {

    private final ExecutionPolicy policy;

    public DockerSandboxRunner(ExecutionPolicy policy) {
        this.policy = policy;
    }

    @Override
    public SandboxResult run(SandboxSpec spec) {
        policy.verify(spec);
        var limits = spec.limits();
        ArrayList<String> command = new ArrayList<>(java.util.List.of(
                "docker", "run", "--rm", "--network", "none", "--read-only",
                "--cpus", Double.toString(limits.cpus()), "--memory", limits.memory(),
                "--pids-limit", Integer.toString(limits.pids()),
                "--user", limits.user(), "-v",
                spec.projectDirectory().toAbsolutePath() + ":/workspace:ro", "-w", "/workspace",
                spec.image()));
        command.addAll(spec.command());
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(false).start();
            StringBuilder stdout = new StringBuilder();
            StringBuilder stderr = new StringBuilder();
            Thread out = Thread.ofVirtual().start(() -> read(process.getInputStream(), stdout));
            Thread err = Thread.ofVirtual().start(() -> read(process.getErrorStream(), stderr));
            boolean completed = process.waitFor(limits.timeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) process.destroyForcibly();
            out.join(1000);
            err.join(1000);
            return new SandboxResult(completed ? process.exitValue() : -1,
                    stdout.toString(), stderr.toString(), !completed);
        } catch (IOException failure) {
            return new SandboxResult(-1, "", failure.getMessage(), false);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return new SandboxResult(-1, "", "Execution interrompue", true);
        }
    }

    private static void read(java.io.InputStream stream, StringBuilder target) {
        try (stream) {
            target.append(new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }
}
