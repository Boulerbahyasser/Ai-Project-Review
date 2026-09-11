package ma.uae.aireviewer.analysis.trace;

import java.util.List;

/** Collecte la trace d'une analyse. Implementation injectee : testable sans effet de bord. */
public interface TraceRecorder {

    void record(TraceEvent event);

    List<TraceEvent> events();
}
