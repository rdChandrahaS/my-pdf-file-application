package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class AttachmentsManagerTool implements Tool {

    @Override
    public String getName() {
        return "Attachments Manager";
    }

    @Override
    public String getDescription() {
        return "Embed file attachments within a PDF document.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-paperclip"; 
    }

    @Override
    public String getIconPath() {
        return "/icons/tools/attachment_pdf.png";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.AttachmentsManagerController.class;
    }
}