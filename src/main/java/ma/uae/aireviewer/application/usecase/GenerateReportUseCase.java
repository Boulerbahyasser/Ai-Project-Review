package ma.uae.aireviewer.application.usecase;

import java.nio.file.Path;
import ma.uae.aireviewer.application.dto.GenerateReportRequest;
import ma.uae.aireviewer.report.ReportWriter;
import ma.uae.aireviewer.report.assembly.ReportAssembler;
import ma.uae.aireviewer.report.compile.PdfCompiler;
import ma.uae.aireviewer.report.render.ReportRendererRegistry;

/** Cas d'utilisation : produire le rapport a partir d'un resultat d'analyse. */
public final class GenerateReportUseCase {

    private final ReportAssembler assembler;
    private final ReportRendererRegistry renderers;
    private final ReportWriter writer;
    private final PdfCompiler pdfCompiler;

    public GenerateReportUseCase(ReportAssembler assembler,
                                 ReportRendererRegistry renderers,
                                 ReportWriter writer,
                                 PdfCompiler pdfCompiler) {
        this.assembler = assembler;
        this.renderers = renderers;
        this.writer = writer;
        this.pdfCompiler = pdfCompiler;
    }

    public Path execute(GenerateReportRequest request) {
        throw new UnsupportedOperationException("TODO");
    }
}
