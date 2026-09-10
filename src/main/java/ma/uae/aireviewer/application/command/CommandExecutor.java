package ma.uae.aireviewer.application.command;

import java.util.concurrent.CompletableFuture;

/** Execute les commandes en arriere-plan pour ne pas bloquer l'IHM. */
public interface CommandExecutor {

    <R> CompletableFuture<R> submit(Command<R> command);
}
