package com.rdchandrahas.core;

import com.rdchandrahas.ui.InjectableController;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

/**
 * NavigationService manages the switching of views within the application's main content area.
 * It handles FXML loading, view injection into the primary StackPane, and dependency injection
 * for controllers that require navigation capabilities.
 */
public class NavigationService {
    private final StackPane contentPane;

    /**
     * Constructs a NavigationService linked to a specific UI container.
     * * @param contentPane The StackPane where new views will be displayed.
     */
    public NavigationService(StackPane contentPane) {
        this.contentPane = contentPane;
    }

    /**
     * Navigates to a standard view defined by an FXML file.
     * * @param fxmlPath The resource path to the FXML layout.
     */
    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            // Inject this navigation service into the controller if applicable
            inject(loader.getController());
            
            // Update the UI container with the new view
            contentPane.getChildren().setAll(view);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * Master Layout Tool Navigation - Dynamically injects tool-specific logic into a shared shell.
     * This allows multiple PDF tools to share the same header/footer structure while swapping
     * out their specific functional logic.
     * * @param controller The tool controller instance to marry with the shared ToolLayout.
     */
    public void navigateToTool(BaseToolController controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ToolLayout.fxml"));
            
            /* * KEY: Manually setting the controller allows the ToolLayout.fxml to be 
             * driven by different logic classes (Rotate, Merge, Split, etc.) without 
             * needing unique FXML files for the container itself.
             */
            loader.setController(controller); 
            
            Parent view = loader.load();
            inject(controller);
            contentPane.getChildren().setAll(view);
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * Internal helper to perform dependency injection on controllers.
     * Checks if a controller implements InjectableController and provides it 
     * with a reference to this NavigationService.
     * * @param controller The controller instance to check and inject.
     */
    private void inject(Object controller) {
        if (controller instanceof InjectableController injectable) {
            injectable.setNavigationService(this);
        }
    }
}