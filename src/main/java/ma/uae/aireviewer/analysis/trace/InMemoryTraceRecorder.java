package ma.uae.aireviewer.analysis.trace;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Trace conservee en memoire pour la duree de l'analyse, puis persistee avec le resultat. */
public final class InMemoryTraceRecorder implements TraceRecorder {

    private final List<TraceEvent> events = new CopyOnWriteArrayList<>();

    @Override
    public void record(TraceEvent event) {
        events.add(event);
    }

    @Override
    public List<TraceEvent> events() {
        return List.copyOf(events);
    }
}
