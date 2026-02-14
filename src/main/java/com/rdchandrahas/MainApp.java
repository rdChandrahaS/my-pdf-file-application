package com.rdchandrahas;

import com.rdchandrahas.core.DefaultOSService;
import com.rdchandrahas.core.OSService;
import com.rdchandrahas.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The MainApp class serves as the core JavaFX Application lifecycle manager.
 * It handles the initialization of the primary UI window, dependency injection 
 * for system services, and global scene management.
 */
public class MainApp extends Application {

    private static Scene scene;

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called by the JavaFX runtime once the system is ready.
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene is set.
     * @throws Exception if the FXML file cannot be loaded or other startup errors occur.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Load the primary FXML layout containing the root interface
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/ui/MainLayout.fxml")
        );
        Parent root = loader.load();

        // 2. Initialize the OS Abstraction Service
        // getHostServices() allows JavaFX to delegate tasks like opening web links to the native OS
        OSService osService = new DefaultOSService(getHostServices());

        // 3. Inject the Service into the MainController
        // This triggers startup tasks like system font scanning and initial UI setup
        MainController controller = loader.getController();
        controller.initService(osService);

        // 4. Setup the Scene and Stage dimensions
        scene = new Scene(root, 1000, 700);

        primaryStage.setTitle("My PDF File Desktop");
        
        /* * SUGGESTION: You can set the application window icon here using your resources.
         * Add the following import: import javafx.scene.image.Image;
         * And uncomment the following lines to add the window icon:
         * * try {
         * primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/pdf.png")));
         * } catch (Exception e) {
         * System.err.println("Could not load application icon.");
         * }
         */

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    /**
     * Globally accessible scene reference.
     * Useful for utility classes that need access to the main window (e.g., FileChoosers, Alerts)
     * without needing to pass the Stage reference around continuously.
     *
     * @return The active root Scene of the application.
     */
    public static Scene getScene() {
        return scene;
    }

    /**
     * Fallback main method.
     * It is standard practice to use Launcher.java to boot the app 
     * to avoid JavaFX module path errors, but this remains required by the Application class.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}