package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

/**
 * UnlockPdfTool provides the registration metadata for the PDF decryption utility.
 * This tool allows users to remove passwords and security restrictions from 
 * protected PDF files, provided they have the necessary authorization.
 */
public class UnlockPdfTool implements Tool {

    /**
     * Returns the functional name of the tool for the dashboard grid.
     * @return "Unlock PDF"
     */
    @Override
    public String getName() {
        return "Unlock PDF";
    }

    /**
     * Provides a brief summary of the tool's function for UI tooltips.
     * @return A description of the decryption and restriction removal features.
     */
    @Override
    public String getDescription() {
        return "Remove passwords and security restrictions from protected PDF documents.";
    }

    /**
     * Points to the specialized FXML view for unlock configurations.
     * @return The resource path for the UnlockView FXML.
     */
    @Override
    public String getFxmlPath() {
        return "/ui/UnlockView.fxml";
    }

    /**
     * Specifies the visual identifier for the tool's icon.
     * @return An unlock icon code from FontAwesome/Ikonli.
     */
    @Override
    public String getIconCode() {
        return "fas-unlock"; 
    }

    /**
     * Explicitly implements the default icon path method for consistency.
     * @return null as it uses font icons.
     */
    @Override
    public String getIconPath() {
        return null;
    }

    /**
     * Returns the specific controller class that manages the logic for unlocking PDFs.
     * @return The UnlockController class type.
     */
    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.UnlockController.class;
    }
}