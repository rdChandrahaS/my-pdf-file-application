package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.core.PdfRedactionService;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PdfRedactionController extends BaseToolController {
    private TextField textToRedactInput;

    @Override
    protected void onInitialize() {
        setTitle("Redact PDF Text");
        setActionText("Redact & Save");
        
        textToRedactInput = new TextField();
        textToRedactInput.setPromptText("Enter text to black out");

        addToolbarItem(new Label("Text to Redact:"), textToRedactInput);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save Redacted PDF", "Redacted_Document.pdf", (dest) -> {
            if (fileListView.getItems().isEmpty()) return;
            String sourcePath = fileListView.getItems().get(0).getPath();
            String textToRedact = textToRedactInput.getText().trim();
            
            PdfRedactionService service = new PdfRedactionService();
            service.redactText(sourcePath, dest.getAbsolutePath(), textToRedact);
        });
    }

    @Override
    protected boolean isInputValid() {
        return !fileListView.getItems().isEmpty() && 
               fileListView.getItems().get(0).getPath().toLowerCase().endsWith(".pdf") &&
               !textToRedactInput.getText().trim().isEmpty();
    }
}