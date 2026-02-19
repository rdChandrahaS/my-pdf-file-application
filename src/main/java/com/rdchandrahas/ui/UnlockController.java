package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.shared.model.FileItem;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnlockController extends BaseToolController {
    
    private static final Logger LOGGER = Logger.getLogger(UnlockController.class.getName());
    private PasswordField pass;

    @Override 
    protected void onInitialize() {
        setTitle("Unlock PDF");
        setActionText("Unlock & Save All");
        pass = new PasswordField(); 
        pass.setPromptText("Enter Current Password");
        pass.setPrefWidth(200);
        pass.textProperty().addListener((obs, oldVal, newVal) -> updateActionBtnState());
        addToolbarItem(new Label("Password:"), pass);
    }

    @Override 
    protected void handleAddFiles() { 
        // Allows adding multiple files
        addFiles("PDF Files", "*.pdf"); 
    }

    @Override 
    protected void handleAction() {
        // 1. Ask the user for a FOLDER to save all the unlocked PDFs
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Destination Folder");
        File destDir = chooser.showDialog(actionBtn.getScene().getWindow());

        if (destDir == null) return;

        setBusy(true, actionBtn);

        // 2. Run the heavy batch process in the background
        new Thread(() -> {
            int successCount = 0;
            
            // 3. Loop through EVERY file the user uploaded
            for (Object obj : fileListView.getItems()) {
                FileItem item = (FileItem) obj;
                File sourceFile = new File(item.getPath());
                
                // Create a new filename (e.g., "document_unlocked.pdf")
                File destFile = new File(destDir, sourceFile.getName().replace(".pdf", "_unlocked.pdf"));

                // 4. Memory-Safe loading and unlocking using the common password
                try (PDDocument doc = loadDocumentSafe(sourceFile.getAbsolutePath(), pass.getText())) {
                    doc.setAllSecurityToBeRemoved(true); 
                    doc.save(destFile);
                    successCount++;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to unlock file: " + sourceFile.getName() + " (Wrong password or corrupted)", e);
                    // Notice we don't throw the error here. This ensures that if ONE file has a different password, 
                    // the loop continues and unlocks the rest of the files!
                }
            }

            final int finalSuccessCount = successCount;
            final int totalFiles = fileListView.getItems().size();

            // 5. Update the UI with the final result
            Platform.runLater(() -> {
                setBusy(false, actionBtn);
                if (finalSuccessCount == totalFiles) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Successfully unlocked all " + finalSuccessCount + " files.");
                } else if (finalSuccessCount > 0) {
                    showAlert(Alert.AlertType.WARNING, "Partial Success", "Unlocked " + finalSuccessCount + " out of " + totalFiles + " files. Check if the password was correct for the ones that failed.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to unlock files. Please verify the password.");
                }
            });
        }).start();
    }

    @Override
    protected boolean isInputValid() {
        return super.isInputValid() && !pass.getText().trim().isEmpty() &&
               fileListView.getItems().stream().allMatch(i -> i.getPath().toLowerCase().endsWith(".pdf"));
    }
}