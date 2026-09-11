package ma.uae.aireviewer.ui;

import com.aireview.llm.LLMProvider;
import com.aireview.llm.MockLLMProvider;
import com.aireview.llm.OllamaProvider;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ma.uae.aireviewer.configuration.UnzipConfig;
import ma.uae.aireviewer.security.archive.ArchiveExtractor;
import ma.uae.aireviewer.security.archive.DockerArchiveExtractor;
import ma.uae.aireviewer.ui.controller.MainController;
import ma.uae.aireviewer.ui.service.EvaluationService;

/**
 * Point d'entree de l'application.
 *
 * <p>C'est ici, et uniquement ici, que le fournisseur de modele est choisi. Le
 * reste de l'interface ne connait que {@link EvaluationService}, ce qui permet de
 * basculer d'un modele local a une doublure de test sans toucher une seule vue
 * (cahier des charges, section 4.1).
 *
 * <p>Le mode est pilote par la propriete systeme {@code aireviewer.provider} :
 * <pre>
 *   mvn javafx:run                                  -&gt; doublure, aucun reseau
 *   mvn javafx:run -Daireviewer.provider=ollama     -&gt; modele local reel
 * </pre>
 */
public class AiReviewerApplication extends Application {

    private static final String DEFAULT_MODEL = "gemma2:2b";
    private static final Path REPORT_DIRECTORY = Path.of("out", "reports");

    /** Reprend les valeurs de la section `unzip:` de config/application.yaml. */
    private static final UnzipConfig UNZIP_CONFIG = new UnzipConfig(
            "ai-reviewer/unzip:latest", "256m", 64, 30, "1000:1000", 200, 5000);

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 760);
        scene.getStylesheets().add(
                getClass().getResource("/css/application.css").toExternalForm());

        ArchiveExtractor archiveExtractor = new DockerArchiveExtractor(UNZIP_CONFIG);

        MainController controller = loader.getController();
        controller.setEvaluationService(new EvaluationService(
                selectProvider(), DEFAULT_MODEL, REPORT_DIRECTORY, archiveExtractor));

        stage.setTitle("AI Project Reviewer");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }

    /**
     * Doublure par defaut : l'application demarre et se demontre sans qu'aucun
     * serveur de modele ne tourne. Le fournisseur reel s'active explicitement.
     */
    private LLMProvider selectProvider() {
        String requested = System.getProperty("aireviewer.provider", "mock");
        return "ollama".equalsIgnoreCase(requested)
                ? new OllamaProvider()
                : new MockLLMProvider();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
