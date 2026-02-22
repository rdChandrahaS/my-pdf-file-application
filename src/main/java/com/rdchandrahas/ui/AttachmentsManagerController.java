package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.core.AttachmentsManagerService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import java.io.File;

public class AttachmentsManagerController extends BaseToolController {
    private String attachmentFilePath;
    private Button selectAttachmentBtn;

    @Override
    protected void onInitialize() {
        setTitle("PDF Attachments Manager");
        setActionText("Attach & Save");
        
        selectAttachmentBtn = new Button("Select File to Attach");
        selectAttachmentBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Attach");
            File file = fileChooser.showOpenDialog(selectAttachmentBtn.getScene().getWindow());
            if (file != null) {
                attachmentFilePath = file.getAbsolutePath();
                selectAttachmentBtn.setText(file.getName());
            }
        });

        addToolbarItem(new Label("Attachment:"), selectAttachmentBtn);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save PDF with Attachment", "Document_With_Attachment.pdf", (dest) -> {
            if (fileListView.getItems().isEmpty() || attachmentFilePath == null) return;
            String sourcePath = fileListView.getItems().get(0).getPath();
            
            AttachmentsManagerService service = new AttachmentsManagerService();
            service.addAttachment(sourcePath, dest.getAbsolutePath(), attachmentFilePath);
        });
    }

    @Override
    protected boolean isInputValid() {
        return !fileListView.getItems().isEmpty() && 
               fileListView.getItems().get(0).getPath().toLowerCase().endsWith(".pdf") &&
               attachmentFilePath != null;
    }
}