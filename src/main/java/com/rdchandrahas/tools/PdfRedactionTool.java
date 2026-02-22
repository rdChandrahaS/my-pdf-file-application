package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class PdfRedactionTool implements Tool {

    @Override
    public String getName() {
        return "Redact PDF";
    }

    @Override
    public String getDescription() {
        return "Search and visually redact (black out) sensitive text in a PDF.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-eraser"; 
    }

    @Override
    public String getIconPath() {
        return "/icons/tools/redact_pdf.png"; 
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.PdfRedactionController.class;
    }
}