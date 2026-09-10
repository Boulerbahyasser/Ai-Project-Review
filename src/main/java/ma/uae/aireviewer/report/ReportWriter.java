package ma.uae.aireviewer.report;

import java.nio.file.Path;

/** Ecrit le rapport rendu sur disque (evaluation.tex). */
public interface ReportWriter {

    Path write(String renderedContent, String fileName);
}
