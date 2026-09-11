package ma.uae.aireviewer.ui.controller;

import com.aireview.llm.CriterionResult;
import com.aireview.llm.EvaluationCriterion;
import com.aireview.llm.EvaluationResult;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ma.uae.aireviewer.ui.dialog.ErrorDialogService;
import ma.uae.aireviewer.ui.mapper.ProjectTreeMapper;
import ma.uae.aireviewer.ui.model.HistoryRow;
import ma.uae.aireviewer.ui.model.ResultRow;
import ma.uae.aireviewer.ui.service.EvaluationService;
import ma.uae.aireviewer.ui.service.ProjectSnapshot;

/**
 * Controleur de la fenetre principale.
 *
 * <p>Il traduit les actions de l'utilisateur en appels a {@link EvaluationService}
 * et n'effectue aucun calcul : ni score, ni parcours du projet analyse, ni appel
 * reseau. La separation entre interface et logique metier est evaluee
 * specifiquement (cahier des charges, section 3.2).
 */
public class MainController {

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private Label projectPathLabel;
    @FXML private TreeView<String> projectTreeView;
    @FXML private ComboBox<String> profileComboBox;
    @FXML private CheckBox architectureCheckBox;
    @FXML private CheckBox solidCheckBox;
    @FXML private CheckBox testingCheckBox;
    @FXML private Button analyzeButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label statusLabel;
    @FXML private TabPane mainTabPane;
    @FXML private Tab resultsTab;

    @FXML private TableView<ResultRow> resultsTable;
    @FXML private TableColumn<ResultRow, String> criterionColumn;
    @FXML private TableColumn<ResultRow, String> scoreColumn;
    @FXML private TableColumn<ResultRow, String> resultStatusColumn;
    @FXML private TextArea resultDetailsArea;
    @FXML private Label overallScoreLabel;
    @FXML private Button reportButton;

    @FXML private TableView<HistoryRow> historyTable;
    @FXML private TableColumn<HistoryRow, String> historyProjectColumn;
    @FXML private TableColumn<HistoryRow, String> historyDateColumn;
    @FXML private TableColumn<HistoryRow, String> historyScoreColumn;
    @FXML private TableColumn<HistoryRow, String> historyModelColumn;

    private final ProjectTreeMapper projectTreeMapper = new ProjectTreeMapper();
    private final ErrorDialogService dialogs = new ErrorDialogService();
    private final List<HistoryRow> historyRows = new ArrayList<>();

    private EvaluationService evaluationService;
    private ProjectSnapshot currentProject;
    private EvaluationResult currentResult;

    @FXML
    private void initialize() {
        profileComboBox.setItems(FXCollections.observableArrayList("Default", "Fast", "Complete"));
        profileComboBox.getSelectionModel().select("Default");

        architectureCheckBox.setSelected(true);
        solidCheckBox.setSelected(true);
        testingCheckBox.setSelected(true);

        analyzeButton.setDisable(true);
        reportButton.setDisable(true);
        progressBar.setProgress(0);

        bind(criterionColumn, ResultRow::getCriterion);
        bind(scoreColumn, ResultRow::getScore);
        bind(resultStatusColumn, ResultRow::getStatus);

        bind(historyProjectColumn, HistoryRow::getProject);
        bind(historyDateColumn, HistoryRow::getDate);
        bind(historyScoreColumn, HistoryRow::getScore);
        bind(historyModelColumn, HistoryRow::getModel);

        resultsTable.getSelectionModel().selectedItemProperty()
                .addListener((observable, previous, selected) -> {
                    if (selected != null) {
                        displayResultDetails(selected.getResult());
                    }
                });
    }

    public void setEvaluationService(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
        refreshHistory();
    }

    // ------------------------------------------------------------ actions

    @FXML
    private void handleSelectProject() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Software Project");

        File selected = chooser.showDialog(analyzeButton.getScene().getWindow());
        if (selected == null) {
            return;
        }

        loadProject(() -> evaluationService.loadProject(selected.toPath()));
    }

    @FXML
    private void handleSelectZip() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Project Archive");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("ZIP Archive", "*.zip"));

        File selected = chooser.showOpenDialog(analyzeButton.getScene().getWindow());
        if (selected == null) {
            return;
        }

        loadProject(() -> evaluationService.loadProjectFromArchive(selected.toPath()));
    }

    /** Factorise l'affichage commun aux deux modes d'import (repertoire ou archive). */
    private void loadProject(java.util.function.Supplier<ProjectSnapshot> loader) {
        try {
            currentProject = loader.get();
            projectPathLabel.setText(currentProject.root().toString());

            TreeItem<String> root = projectTreeMapper.toTreeItem(currentProject.root());
            root.setExpanded(true);
            projectTreeView.setRoot(root);

            analyzeButton.setDisable(false);
            reportButton.setDisable(true);
            currentResult = null;
            statusLabel.setText(currentProject.fileCount()
                    + " fichier(s) Java charge(s) depuis " + currentProject.name());
        } catch (RuntimeException failure) {
            dialogs.showError("Project Import Error", failure.getMessage());
        }
    }

    @FXML
    private void handleStartAnalysis() {
        if (currentProject == null) {
            dialogs.showError("No Project", "Please select a project first.");
            return;
        }

        List<EvaluationCriterion> criteria = selectedCriteria();
        if (criteria.isEmpty()) {
            dialogs.showError("No Criteria", "Select at least one evaluation criterion.");
            return;
        }

        setBusy(true);
        resultsTable.getItems().clear();
        resultDetailsArea.clear();
        overallScoreLabel.setText("Overall score: -");
        statusLabel.setText("Analysis running...");

        Task<EvaluationResult> task = new Task<>() {
            @Override
            protected EvaluationResult call() throws Exception {
                return evaluationService.evaluate(currentProject, criteria,
                        (completed, total, message) -> {
                            updateProgress(completed, total);
                            updateMessage(message);
                        });
            }
        };

        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(event -> {
            unbindProgress();
            progressBar.setProgress(1);
            progressLabel.setText("Analysis completed.");

            currentResult = task.getValue();
            showResult(currentResult);
            recordHistory(currentResult);

            setBusy(false);
            reportButton.setDisable(false);
            statusLabel.setText("Analysis completed in "
                    + currentResult.durationMillis() + " ms.");
            mainTabPane.getSelectionModel().select(resultsTab);
        });

        task.setOnFailed(event -> {
            unbindProgress();
            progressBar.setProgress(0);
            progressLabel.setText("Analysis failed.");
            setBusy(false);
            statusLabel.setText("Analysis failed.");

            Throwable cause = task.getException();
            dialogs.showError("Analysis Error",
                    cause == null ? "Unknown error" : cause.getMessage());
        });

        Thread worker = new Thread(task, "analysis-task");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void handleGenerateReport() {
        if (currentResult == null) {
            dialogs.showError("No Result", "Run an analysis before generating a report.");
            return;
        }

        try {
            var path = evaluationService.generateReport(currentResult);
            dialogs.showInformation("Report Generated", "LaTeX report saved to:\n" + path);
            statusLabel.setText("Report generated: " + path.getFileName());
        } catch (Exception failure) {
            dialogs.showError("Report Generation Error", failure.getMessage());
        }
    }

    // ------------------------------------------------------------ affichage

    private List<EvaluationCriterion> selectedCriteria() {
        List<EvaluationCriterion> criteria = new ArrayList<>();
        if (architectureCheckBox.isSelected()) {
            criteria.add(EvaluationCriterion.ARCHITECTURE);
        }
        if (solidCheckBox.isSelected()) {
            criteria.add(EvaluationCriterion.SOLID_PRINCIPLES);
        }
        if (testingCheckBox.isSelected()) {
            criteria.add(EvaluationCriterion.TESTING);
        }
        return criteria;
    }

    private void showResult(EvaluationResult result) {
        var rows = result.results().stream().map(ResultRow::new).toList();
        resultsTable.setItems(FXCollections.observableArrayList(rows));

        overallScoreLabel.setText(String.format("Overall score: %d/%d (%.0f%%)",
                result.totalScore(), result.maxTotalScore(), result.overallPercent()));

        if (!rows.isEmpty()) {
            resultsTable.getSelectionModel().selectFirst();
        }
    }

    private void displayResultDetails(CriterionResult result) {
        StringBuilder details = new StringBuilder();
        details.append(result.criterion())
                .append(" - ")
                .append(result.score()).append("/").append(result.maxScore())
                .append(String.format(" (%.0f%%)", result.scorePercent()))
                .append("\n\n");

        details.append("FEEDBACK\n")
                .append(result.feedback() == null || result.feedback().isBlank()
                        ? "No feedback provided." : result.feedback())
                .append("\n\n");

        details.append("ISSUES\n");
        if (result.issues().isEmpty()) {
            details.append("- None\n");
        } else {
            result.issues().forEach(issue -> details.append("- ").append(issue).append("\n"));
        }

        resultDetailsArea.setText(details.toString());
    }

    private void recordHistory(EvaluationResult result) {
        historyRows.add(0, new HistoryRow(
                result, evaluationService.getModel(), LocalDateTime.now().format(TIMESTAMP)));
        refreshHistory();
    }

    private void refreshHistory() {
        historyTable.setItems(FXCollections.observableArrayList(historyRows));
    }

    private void unbindProgress() {
        progressBar.progressProperty().unbind();
        progressLabel.textProperty().unbind();
    }

    private void setBusy(boolean busy) {
        analyzeButton.setDisable(busy || currentProject == null);
        reportButton.setDisable(busy || currentResult == null);
        projectTreeView.setDisable(busy);
        profileComboBox.setDisable(busy);
        architectureCheckBox.setDisable(busy);
        solidCheckBox.setDisable(busy);
        testingCheckBox.setDisable(busy);
    }

    /** Evite de repeter une fabrique de valeur de cellule pour chaque colonne. */
    private <T> void bind(TableColumn<T, String> column,
                          java.util.function.Function<T, String> accessor) {
        column.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(accessor.apply(data.getValue())));
    }
}
