package ma.uae.aireviewer.application.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Diffuse les evenements aux abonnes. Le moteur ne connait pas l'IHM. */
public final class AnalysisEventPublisher {

    private final List<AnalysisListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(AnalysisListener listener) {
        if (listener != null && !listeners.contains(listener)) listeners.add(listener);
    }

    public void unsubscribe(AnalysisListener listener) {
        listeners.remove(listener);
    }

    public void publish(AnalysisEvent event) {
        listeners.forEach(listener -> listener.onEvent(event));
    }
}
