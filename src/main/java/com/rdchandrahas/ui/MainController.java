package com.rdchandrahas.ui;

import com.rdchandrahas.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.application.Platform;

public class MainController {

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        loadView("Dashboard.fxml");
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void setLightTheme() {
        MainApp.getScene().getStylesheets().clear();
        MainApp.getScene().getStylesheets().add(
                getClass().getResource("/css/light.css").toExternalForm()
        );
    }

    @FXML
    private void setDarkTheme() {
        MainApp.getScene().getStylesheets().clear();
        MainApp.getScene().getStylesheets().add(
                getClass().getResource("/css/dark.css").toExternalForm()
        );
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About PDFGear");
        alert.setHeaderText("PDFGear Desktop");
        alert.setContentText("Offline PDF processing tool built with JavaFX.");
        alert.showAndWait();
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/" + fxml)
            );
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
