package com.rdchandrahas.ui.base;

import com.rdchandrahas.core.ExecutionManager;
import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.ui.InjectableController;
import com.rdchandrahas.ui.SortableToolController;
import com.rdchandrahas.shared.component.FileListView;
import com.rdchandrahas.shared.model.FileItem;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * BaseToolController serves as the foundation for all PDF tool controllers.
 * It provides common UI management, asynchronous task execution, and 
 * safe PDF handling methods to ensure consistency across the application.
 */
public abstract class BaseToolController implements SortableToolController, InjectableController {

    @FXML protected Label toolTitleLabel;
    @FXML protected Button addFilesBtn;
    @FXML protected HBox customToolbarArea; 
    @FXML protected FileListView fileListView;
    @FXML protected ComboBox<String> sortCombo;
    @FXML protected ToggleButton listViewBtn;
    @FXML protected ToggleButton gridViewBtn;
    @FXML protected ProgressIndicator progressIndicator;
    @FXML protected Button actionBtn;

    protected NavigationService navigationService;

    @FXML
    public void initialize() {
        setupSortAndViews();
        onInitialize(); 
    }

    /**
     * Hook for subclasses to perform specific initialization logic 
     * after the base UI setup is complete.
     */
    protected abstract void onInitialize();

    // --- UI Configuration Helpers ---
    protected void setTitle(String title) { toolTitleLabel.setText(title); }
    protected void setActionText(String text) { actionBtn.setText(text); }
    protected void addToolbarItem(Node... nodes) { customToolbarArea.getChildren().addAll(nodes); }

    /**
     * Opens a system FileChooser and adds selected files to the shared list.
     * @param filterName Description of the allowed files.
     * @param extensions File extensions (e.g., "*.pdf", "*.jpg").
     */
    protected void addFiles(String filterName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select " + filterName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(filterName, extensions));
        List<File> files = chooser.showOpenMultipleDialog(addFilesBtn.getScene().getWindow());
        
        if (files != null) {
            for (File file : files) {
                fileListView.getItems().add(new FileItem(file.getAbsolutePath()));
            }
        }
    }

    // --- Abstract Handlers ---
    @FXML protected abstract void handleAddFiles();
    @FXML protected abstract void handleAction();

    // --- Common UI Handlers ---
    @FXML protected void handleBack() { navigationService.navigateTo("/ui/Dashboard.fxml"); }
    @FXML protected void onSortAction() { handleSort(); }
    @FXML protected void onListToggle() { switchToList(); }
    @FXML protected void onGridToggle() { switchToGrid(); }
    @FXML protected void handleRemove() { fileListView.getItems().clear(); }

    /**
     * Standardized workflow for processing PDF tasks.
     * Opens a save dialog, runs the task on a background thread, and handles UI updates.
     */
    protected void processWithSaveDialog(String title, String defaultName, ToolTask task) {
        if (fileListView.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No files", "Please add files first.");
            return;
        }
        
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(defaultName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File dest = chooser.showSaveDialog(actionBtn.getScene().getWindow());

        if (dest == null) return;

        setBusy(true, actionBtn);
        ExecutionManager.submit(() -> {
            try {
                task.execute(dest);
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Saved to: " + dest.getName());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setBusy(false, actionBtn);
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                });
            }
        });
    }

    // --- PDF Safe Methods (Memory Optimized) ---
    protected PDDocument loadDocumentSafe(String path) throws IOException {
        return PDDocument.load(new File(path), MemoryUsageSetting.setupTempFileOnly());
    }
    
    protected PDDocument loadDocumentSafe(String path, String pass) throws IOException {
        return PDDocument.load(new File(path), pass, MemoryUsageSetting.setupTempFileOnly());
    }
    
    protected PDDocument createDocumentSafe() {
        return new PDDocument(MemoryUsageSetting.setupTempFileOnly());
    }
    
    protected void mergeDocumentsSafe(List<String> paths, File dest) throws IOException {
        PDFMergerUtility m = new PDFMergerUtility();
        m.setDestinationFileName(dest.getAbsolutePath());
        for (String p : paths) m.addSource(new File(p));
        m.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
    }

    // --- Interface Implementations ---
    @Override public FileListView getFileListView() { return fileListView; }
    @Override public ComboBox<String> getSortCombo() { return sortCombo; }
    @Override public ToggleButton getListViewBtn() { return listViewBtn; }
    @Override public ToggleButton getGridViewBtn() { return gridViewBtn; }
    @Override public void setNavigationService(NavigationService nav) { this.navigationService = nav; }

    /**
     * Toggles the busy state of the UI, showing the progress indicator 
     * and disabling the primary action button.
     */
    protected void setBusy(boolean b, Button btn) {
        progressIndicator.setVisible(b);
        btn.setDisable(b);
    }

    /** Helper to show JavaFX alerts. */
    protected void showAlert(Alert.AlertType t, String title, String content) {
        Alert a = new Alert(t); 
        a.setTitle(title); 
        a.setHeaderText(null); 
        a.setContentText(content); 
        a.show();
    }

    /** Functional interface for background PDF operations. */
    @FunctionalInterface 
    public interface ToolTask { 
        void execute(File destination) throws Exception; 
    }
}