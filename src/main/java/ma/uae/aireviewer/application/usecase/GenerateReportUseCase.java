package ma.uae.aireviewer.application.usecase;

import java.nio.file.Path;
import ma.uae.aireviewer.application.dto.GenerateReportRequest;
import ma.uae.aireviewer.report.ReportWriter;
import ma.uae.aireviewer.report.assembly.ReportAssembler;
import ma.uae.aireviewer.report.compile.PdfCompiler;
import ma.uae.aireviewer.report.render.ReportRendererRegistry;
import ma.uae.aireviewer.analysis.result.AnalysisResult;

/** Cas d'utilisation : produire le rapport a partir d'un resultat d'analyse. */
public final class GenerateReportUseCase {

    private final ReportAssembler assembler;
    private final ReportRendererRegistry renderers;
    private final ReportWriter writer;
    private final PdfCompiler pdfCompiler;
    private final AnalysisResultStore results;

    public GenerateReportUseCase(ReportAssembler assembler,
                                 ReportRendererRegistry renderers,
                                 ReportWriter writer,
                                 PdfCompiler pdfCompiler) {
        this(assembler, renderers, writer, pdfCompiler, AnalysisResultStore.DEFAULT);
    }

    public GenerateReportUseCase(ReportAssembler assembler,
                                 ReportRendererRegistry renderers,
                                 ReportWriter writer,
                                 PdfCompiler pdfCompiler,
                                 AnalysisResultStore results) {
        this.assembler = assembler;
        this.renderers = renderers;
        this.writer = writer;
        this.pdfCompiler = pdfCompiler;
        this.results = results;
    }

    public Path execute(GenerateReportRequest request) {
        if (request == null || request.analysisId() == null || request.analysisId().isBlank()) {
            throw new IllegalArgumentException("Analyse obligatoire");
        }
        AnalysisResult result = results.find(request.analysisId())
                .orElseThrow(() -> new IllegalArgumentException("Analyse introuvable : " + request.analysisId()));
        String format = request.format() == null || request.format().isBlank() ? "latex" : request.format();
        var rendered = renderers.byFormat(format).render(assembler.assemble(result));
        String extension = "html".equalsIgnoreCase(format) ? ".html" : ".tex";
        Path output = writer.write(rendered, "evaluation-" + request.analysisId() + extension);
        if (request.compilePdf() && "latex".equalsIgnoreCase(format)) {
            pdfCompiler.compile(output);
        }
        return output;
    }
}
