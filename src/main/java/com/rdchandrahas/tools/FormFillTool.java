package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class FormFillTool implements Tool {

    @Override
    public String getName() {
        return "Fill Form";
    }

    @Override
    public String getDescription() {
        return "Automatically fill interactive PDF forms.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-edit"; 
    }

    @Override
    public String getIconPath() {
        return "/icons/tools/form_fill_pdf.png";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.FormFillController.class;
    }
}