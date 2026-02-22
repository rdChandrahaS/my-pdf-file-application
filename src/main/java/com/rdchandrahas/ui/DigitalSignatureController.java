package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.core.DigitalSignatureService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.FileChooser;
import java.io.File;

public class DigitalSignatureController extends BaseToolController {
    private String keyStorePath;
    private PasswordField passwordField;
    private Button selectKeyStoreBtn;

    @Override
    protected void onInitialize() {
        setTitle("Digitally Sign PDF");
        setActionText("Sign PDF");
        
        selectKeyStoreBtn = new Button("Select Keystore (.p12)");
        selectKeyStoreBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("KeyStore Files", "*.p12", "*.pfx"));
            File file = fileChooser.showOpenDialog(selectKeyStoreBtn.getScene().getWindow());
            if (file != null) {
                keyStorePath = file.getAbsolutePath();
                selectKeyStoreBtn.setText(file.getName());
            }
        });

        passwordField = new PasswordField();
        passwordField.setPromptText("Keystore Password");

        addToolbarItem(new Label("Keystore:"), selectKeyStoreBtn);
        addToolbarItem(new Label("Password:"), passwordField);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save Signed PDF", "Signed_Document.pdf", (dest) -> {
            if (fileListView.getItems().isEmpty() || keyStorePath == null) return;
            String sourcePath = fileListView.getItems().get(0).getPath();
            String password = passwordField.getText();
            
            DigitalSignatureService service = new DigitalSignatureService();
            service.signPdf(sourcePath, dest.getAbsolutePath(), keyStorePath, password);
        });
    }

    @Override
    protected boolean isInputValid() {
        return !fileListView.getItems().isEmpty() && 
               fileListView.getItems().get(0).getPath().toLowerCase().endsWith(".pdf") && 
               keyStorePath != null && 
               !passwordField.getText().isEmpty();
    }
}