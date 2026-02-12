package com.rdchandrahas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/MainLayout.fxml")
        );

        scene = new Scene(loader.load(), 800, 500);

        primaryStage.setTitle("PDFGear Desktop");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static Scene getScene() {
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
