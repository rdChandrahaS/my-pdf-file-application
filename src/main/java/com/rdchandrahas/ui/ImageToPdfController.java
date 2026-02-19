package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.core.ImageToPdfService;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.util.List;
import java.util.stream.Collectors;

public class ImageToPdfController extends BaseToolController {
    private ComboBox<String> layout;

    @Override
    protected void onInitialize() {
        setTitle("Image to PDF");
        setActionText("Convert & Save");
        layout = new ComboBox<>();
        layout.getItems().addAll("Portrait", "Landscape", "Original");
        layout.setValue("Portrait");
        addToolbarItem(new Label("Layout:"), layout);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("Images", "*.jpg", "*.png", "*.webp", "*.jpeg");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save PDF", "Images.pdf", (dest) -> {
            // FIX: Delegate to the memory-safe ImageToPdfService instead of processing in RAM
            List<String> imagePaths = fileListView.getItems().stream()
                    .map(FileItem::getPath)
                    .collect(Collectors.toList());
            
            ImageToPdfService service = new ImageToPdfService();
            service.convertImagesToPdf(imagePaths, dest.getAbsolutePath());
        });
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty()) {
            return false;
        }
        for (FileItem item : fileListView.getItems()) {
            String path = item.getPath().toLowerCase();
            if (!path.endsWith(".jpg") && !path.endsWith(".jpeg") && !path.endsWith(".png") && !path.endsWith(".webp")) {
                return false; 
            }
        }
        return true;
    }
}