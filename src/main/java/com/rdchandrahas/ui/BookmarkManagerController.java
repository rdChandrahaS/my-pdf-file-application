package com.rdchandrahas.ui;

import com.rdchandrahas.ui.base.BaseToolController;
import com.rdchandrahas.core.BookmarkManagerService;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class BookmarkManagerController extends BaseToolController {
    private TextArea bookmarksInput;

    @Override
    protected void onInitialize() {
        setTitle("Bookmark Manager");
        setActionText("Add Bookmarks & Save");
        
        bookmarksInput = new TextArea();
        bookmarksInput.setPromptText("Format -> PageNumber, Bookmark Title\nExample:\n1, Introduction\n5, Chapter 1\n12, Conclusion");
        bookmarksInput.setPrefRowCount(8);

        VBox container = new VBox(5);
        container.getChildren().addAll(new Label("Bookmarks:"), bookmarksInput);
        addToolbarItem(container);
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        processWithSaveDialog("Save Bookmarked PDF", "Bookmarked_Document.pdf", (dest) -> {
            if (fileListView.getItems().isEmpty()) return;
            String sourcePath = fileListView.getItems().get(0).getPath();
            
            List<String> lines = List.of(bookmarksInput.getText().split("\\n"));
            List<BookmarkManagerService.BookmarkEntry> entries = new ArrayList<>();
            
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    try {
                        int page = Integer.parseInt(parts[0].trim());
                        String title = parts[1].trim();
                        entries.add(new BookmarkManagerService.BookmarkEntry(page, title));
                    } catch (NumberFormatException ignored) {}
                }
            }
            
            BookmarkManagerService service = new BookmarkManagerService();
            service.addBookmarks(sourcePath, dest.getAbsolutePath(), entries);
        });
    }

    @Override
    protected boolean isInputValid() {
        return !fileListView.getItems().isEmpty() && 
               fileListView.getItems().get(0).getPath().toLowerCase().endsWith(".pdf") &&
               !bookmarksInput.getText().trim().isEmpty();
    }
}