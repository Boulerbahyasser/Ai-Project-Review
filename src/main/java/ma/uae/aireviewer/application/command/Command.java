package ma.uae.aireviewer.application.command;

/** Pattern Command : operation executable hors du thread IHM. */
public interface Command<R> {

    String name();

    R execute();
}
