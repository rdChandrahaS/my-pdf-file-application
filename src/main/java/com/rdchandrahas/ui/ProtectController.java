package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.shared.model.FileItem;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProtectController extends BaseToolController {
    
    private static final Logger LOGGER = Logger.getLogger(ProtectController.class.getName());
    private PasswordField pass;

    @Override 
    protected void onInitialize() {
        setTitle("Protect PDF");
        setActionText("Encrypt & Save All");
        
        pass = new PasswordField(); 
        pass.setPromptText("Enter Password");
        pass.setPrefWidth(200);
        pass.textProperty().addListener((obs, oldVal, newVal) -> updateActionBtnState());
        
        addToolbarItem(new Label("Password:"), pass);
    }

    @Override 
    protected void handleAddFiles() { 
        addFiles("PDF Files", "*.pdf"); 
    }

    @Override 
    protected void handleAction() {
        // FIX: Upgraded to batch processing via DirectoryChooser
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Destination Folder");
        File destDir = chooser.showDialog(actionBtn.getScene().getWindow());

        if (destDir == null) return;

        setBusy(true, actionBtn);

        new Thread(() -> {
            int successCount = 0;
            String password = pass.getText();
            
            for (Object obj : fileListView.getItems()) {
                FileItem item = (FileItem) obj;
                File sourceFile = new File(item.getPath());
                File destFile = new File(destDir, sourceFile.getName().replace(".pdf", "_protected.pdf"));

                try {
                    // Safe execution pipeline
                    processPdfSafely(sourceFile, destFile, (doc) -> {
                        StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, new AccessPermission());
                        spp.setEncryptionKeyLength(128); 
                        doc.protect(spp); 
                    });
                    successCount++;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to encrypt file: " + sourceFile.getName(), e);
                }
            }

            final int finalSuccessCount = successCount;
            final int totalFiles = fileListView.getItems().size();

            Platform.runLater(() -> {
                setBusy(false, actionBtn);
                if (finalSuccessCount == totalFiles) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully encrypted all " + finalSuccessCount + " files.");
                } else if (finalSuccessCount > 0) {
                    showAlert(Alert.AlertType.WARNING, "Partial Success", "Encrypted " + finalSuccessCount + " out of " + totalFiles + " files.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to encrypt files.");
                }
            });
        }).start();
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty() || pass.getText().trim().isEmpty()) return false;
        for (FileItem item : fileListView.getItems()) {
            if (!item.getPath().toLowerCase().endsWith(".pdf")) return false;
        }
        return true;
    }
}