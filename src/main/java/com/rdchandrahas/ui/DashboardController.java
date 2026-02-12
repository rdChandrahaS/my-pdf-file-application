package com.rdchandrahas.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class DashboardController {

    @FXML
    private StackPane contentPane;

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/" + fxml)
            );
            Parent view = loader.load();

            // Get root layout and replace center
            StackPane rootPane = (StackPane) view.getParent();
            if (rootPane != null) {
                rootPane.getChildren().setAll(view);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openMergeView() {
        System.out.println("Merge clicked");
    }

    @FXML
    private void openImageView() {
        System.out.println("Image → PDF clicked");
    }

    @FXML
    private void openSplitView() {
        System.out.println("Split clicked");
    }

    @FXML
    private void openCompressView() {
        System.out.println("Compress clicked");
    }
}
