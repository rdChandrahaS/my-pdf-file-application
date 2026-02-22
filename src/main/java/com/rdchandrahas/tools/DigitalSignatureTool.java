package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class DigitalSignatureTool implements Tool {

    @Override
    public String getName() {
        return "Digital Signature";
    }

    @Override
    public String getDescription() {
        return "Digitally sign PDF documents.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-file-signature"; 
    }

    @Override
    public String getIconPath() {
        return "/icons/tools/signature_pdf.png";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.DigitalSignatureController.class;
    }
}