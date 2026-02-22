package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class MetadataEditorTool implements Tool {

    @Override
    public String getName() {
        return "Metadata Editor";
    }

    @Override
    public String getDescription() {
        return "View and edit PDF document properties (Title, Author, Subject, etc.).";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-tags"; 
    }

    @Override
    public String getIconPath() {
        return "/icons/tools/metadata_pdf.png"; // Make sure to add an icon here
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.MetadataEditorController.class;
    }
}