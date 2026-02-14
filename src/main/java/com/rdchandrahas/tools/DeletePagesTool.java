package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * DeletePagesTool provides the registration metadata for the page removal utility.
 * It integrates into the ToolRegistry to allow users to select and launch 
 * the page deletion interface.
 */
public class DeletePagesTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Delete Pages"
     */
    @Override
    public String getName() {
        return "Delete Pages";
    }

    /**
     * Explains the purpose of the tool, specifically focusing on page manipulation.
     * @return A brief summary of the tool's function.
     */
    @Override
    public String getDescription() {
        return "Remove specific pages from your PDF document.";
    }

    /**
     * Points to the shared FXML container that hosts the tool's specific logic.
     * @return The resource path for the tool shell.
     */
    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    /**
     * Specifies the visual identifier for the tool's icon.
     * @return A minus-circle icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() {
        return "fas-minus-circle";
    }

    /**
     * Explicitly implements the default icon path method to ensure consistency.
     * @return null as it uses font icons by default.
     */
    @Override
    public String getIconPath() {
        return null;
    }

    /**
     * Returns the specific controller class that manages the logic for deleting pages.
     * @return The DeletePagesController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.DeletePagesController.class;
    }
}