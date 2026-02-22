package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.core.FormFillService;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.HashMap;
import java.util.Map;

public class FormFillController extends BaseToolController {
    private TextField fieldNameInput;
    private TextField fieldValueInput;

    @Override
    protected void onInitialize() {
        setTitle("Fill PDF Form");
        setActionText("Fill & Save");
        
        fieldNameInput = new TextField();
        fieldNameInput.setPromptText("Enter Field Name");
        
        fieldValueInput = new TextField();
        fieldValueInput.setPromptText("Enter Value");

        addToolbarItem(new Label("Field:"), fieldNameInput);
        addToolbarItem(new Label("Value:"), fieldValueInput);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save Filled PDF", "Filled_Form.pdf", (dest) -> {
            if (fileListView.getItems().isEmpty()) return;
            String sourcePath = fileListView.getItems().get(0).getPath();
            
            Map<String, String> fieldData = new HashMap<>();
            String key = fieldNameInput.getText().trim();
            String value = fieldValueInput.getText().trim();
            
            if (!key.isEmpty()) {
                fieldData.put(key, value);
            }
            
            FormFillService service = new FormFillService();
            service.fillForm(sourcePath, dest.getAbsolutePath(), fieldData);
        });
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty()) {
            return false;
        }
        String path = fileListView.getItems().get(0).getPath().toLowerCase();
        return path.endsWith(".pdf");
    }
}