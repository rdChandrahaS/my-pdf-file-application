package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.model.ViewMode;
import com.rdchandrahas.shared.util.PdfThumbnailUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import java.io.File;

/**
 * FileListView is a hybrid component that supports displaying files in either 
 * a traditional list view or a visual tile/grid view.
 */
public class FileListView extends StackPane {

    private final ObservableList<FileItem> items = FXCollections.observableArrayList();
    private final ListView<FileItem> listView = new ListView<>(items);
    private final TilePane gridPane = new TilePane();
    private final ScrollPane gridScroll = new ScrollPane(gridPane);
    
    // KEEP: We keep this variable to track state
    private ViewMode currentMode = ViewMode.LIST;

    public FileListView() {
        // --- Grid Layout Configuration ---
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setPrefTileWidth(180);
        gridPane.setPrefTileHeight(240);
        gridPane.setAlignment(Pos.TOP_LEFT);
        
        gridPane.getStyleClass().add("grid-pane-container");

        gridScroll.setFitToWidth(true);
        gridScroll.setPannable(true);
        gridScroll.getStyleClass().add("grid-scroll");

        getChildren().addAll(listView, gridScroll);

        setupListView();
        setupGridView();
        setupDesktopDropSupport();
        
        setViewMode(ViewMode.LIST);
    }

    private void setupListView() {
        listView.setCellFactory(lv -> new FileListCell());
    }

    private void setupGridView() {
        items.addListener((javafx.collections.ListChangeListener<FileItem>) c -> refreshGrid());
        refreshGrid();
    }

    private void refreshGrid() {
        gridPane.getChildren().clear();
        for (int i = 0; i < items.size(); i++) {
            FileItem item = items.get(i);
            VBox card = createGridCard(item, i);
            gridPane.getChildren().add(card);
        }
    }

    private VBox createGridCard(FileItem item, int index) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(170);
        
        card.getStyleClass().add("grid-card");

        ImageView image = new ImageView();
        image.setFitWidth(150);
        image.setFitHeight(200);
        image.setPreserveRatio(true); 

        Label name = new Label(item.getName());
        name.setWrapText(true);
        name.setMaxWidth(160);
        name.setAlignment(Pos.CENTER);

        PdfThumbnailUtil.loadThumbnailAsync(item.getPath(), image::setImage);
        
        card.getChildren().addAll(image, name);
        enableDragReorderGrid(card, index);
        
        return card;
    }

    private void setupDesktopDropSupport() {
        this.setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        this.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".pdf") || name.endsWith(".jpg") || name.endsWith(".png")) {
                        items.add(new FileItem(file.getAbsolutePath()));
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void enableDragReorderGrid(VBox card, int index) {
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(index));
            db.setContent(content);
            event.consume();
        });

        card.setOnDragOver(event -> {
            if (event.getGestureSource() != card && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        card.setOnDragDropped(event -> {
            try {
                int draggedIndex = Integer.parseInt(event.getDragboard().getString());
                FileItem draggedItem = items.remove(draggedIndex);
                items.add(index, draggedItem);
                event.setDropCompleted(true);
            } catch (Exception e) {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    /**
     * Switches the UI between LIST view and GRID view.
     */
    public void setViewMode(ViewMode mode) {
        this.currentMode = mode; // Assigned here
        listView.setVisible(mode == ViewMode.LIST);
        gridScroll.setVisible(mode == ViewMode.GRID);
    }

    /**
     * NEW: Returns the current view mode.
     * This fixes the "unused variable" warning because we are now reading it.
     */
    public ViewMode getViewMode() {
        return currentMode;
    }

    public ObservableList<FileItem> getItems() { return items; }
    
    public FileItem getSelectedItem() { return listView.getSelectionModel().getSelectedItem(); }
    
    public void sortByName(boolean ascending) {
        items.sort((a, b) -> ascending ? a.getName().compareToIgnoreCase(b.getName()) : b.getName().compareToIgnoreCase(a.getName()));
    }

    public void sortBySize(boolean ascending) {
        items.sort((a, b) -> {
            int result = Long.compare(a.getSize(), b.getSize());
            return ascending ? result : -result;
        });
    }
}