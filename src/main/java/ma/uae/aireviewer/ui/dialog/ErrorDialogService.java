package ma.uae.aireviewer.ui.dialog;

import javafx.scene.control.Alert;

public final class ErrorDialogService {

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null || message.isBlank()
                ? "An unexpected error occurred."
                : message);
        alert.showAndWait();
    }

    public void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
