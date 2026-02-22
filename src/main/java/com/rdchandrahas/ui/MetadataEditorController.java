package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.core.MetadataEditorService;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.HashMap;
import java.util.Map;

public class MetadataEditorController extends BaseToolController {
    private TextField titleInput;
    private TextField authorInput;
    private TextField subjectInput;
    private TextField keywordsInput;

    @Override
    protected void onInitialize() {
        setTitle("Edit PDF Metadata");
        setActionText("Update & Save");
        
        titleInput = new TextField();
        titleInput.setPromptText("New Title");
        
        authorInput = new TextField();
        authorInput.setPromptText("New Author");

        subjectInput = new TextField();
        subjectInput.setPromptText("New Subject");

        keywordsInput = new TextField();
        keywordsInput.setPromptText("New Keywords");

        addToolbarItem(new Label("Title:"), titleInput);
        addToolbarItem(new Label("Author:"), authorInput);
        addToolbarItem(new Label("Subject:"), subjectInput);
        addToolbarItem(new Label("Keywords:"), keywordsInput);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save Updated PDF", "Updated_Metadata.pdf", (dest) -> {
            if (fileListView.getItems().isEmpty()) return;
            String sourcePath = fileListView.getItems().get(0).getPath();
            
            Map<String, String> metadata = new HashMap<>();
            if (!titleInput.getText().trim().isEmpty()) metadata.put("Title", titleInput.getText().trim());
            if (!authorInput.getText().trim().isEmpty()) metadata.put("Author", authorInput.getText().trim());
            if (!subjectInput.getText().trim().isEmpty()) metadata.put("Subject", subjectInput.getText().trim());
            if (!keywordsInput.getText().trim().isEmpty()) metadata.put("Keywords", keywordsInput.getText().trim());
            
            MetadataEditorService service = new MetadataEditorService();
            service.updateMetadata(sourcePath, dest.getAbsolutePath(), metadata);
        });
    }

    @Override
    protected boolean isInputValid() {
        return !fileListView.getItems().isEmpty() && 
               fileListView.getItems().get(0).getPath().toLowerCase().endsWith(".pdf");
    }
}