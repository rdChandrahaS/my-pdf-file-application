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
import java.util.ArrayList;
import java.util.List;

/**
 * FileListView is a hybrid component that supports displaying files in either 
 * a traditional list view or a visual tile/grid view.
 */
public class FileListView extends StackPane {

    private final ObservableList<FileItem> items = FXCollections.observableArrayList();
    private final ListView<FileItem> listView = new ListView<>(items);
    private final TilePane gridPane = new TilePane();
    private final ScrollPane gridScroll = new ScrollPane(gridPane);
    
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
        
        // MEMORY PROTECTION: TilePane is not virtualized. Attempting to render 5,000 
        // ImageViews simultaneously will cause an OutOfMemory error.
        // We limit the grid to 500, but the ListView can handle unlimited files safely.
        int displayLimit = Math.min(items.size(), 500);
        List<VBox> newCards = new ArrayList<>();

        for (int i = 0; i < displayLimit; i++) {
            FileItem item = items.get(i);
            newCards.add(createGridCard(item, i));
        }
        
        // FIX: Batch Add to UI to prevent stuttering
        gridPane.getChildren().addAll(newCards);

        // Notify user if grid is truncated
        if (items.size() > displayLimit) {
            Label warning = new Label("Showing first " + displayLimit + " thumbnails. Switch to List View to see all " + items.size() + " files.");
            warning.setStyle("-fx-font-weight: bold; -fx-text-fill: #ff6b6b; -fx-padding: 20;");
            gridPane.getChildren().add(warning);
        }
    }

    private VBox createGridCard(FileItem item, int index) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        
        // FIX: Strictly lock dimensions to prevent massive CPU calculations during window resize
        card.setPrefSize(170, 240);
        card.setMinSize(170, 240);
        card.setMaxSize(170, 240);
        card.getStyleClass().add("grid-card");

        ImageView image = new ImageView();
        image.setFitWidth(150);
        image.setFitHeight(180);
        image.setPreserveRatio(true); 

        Label name = new Label(item.getName());
        // FIX: Removed 'name.setWrapText(true)' which causes the JavaFX Layout engine to crash on resize.
        name.setWrapText(false);
        name.setTextOverrun(OverrunStyle.ELLIPSIS); // Cuts off long text safely with "..."
        name.setPrefWidth(160);
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
                // FIX: Batch Drag-and-Drop addition
                List<FileItem> droppedItems = new ArrayList<>();
                for (File file : db.getFiles()) {
                    String name = file.getName().toLowerCase();
                    if (name.endsWith(".pdf") || name.endsWith(".jpg") || name.endsWith(".png")) {
                        droppedItems.add(new FileItem(file.getAbsolutePath()));
                    }
                }
                
                if (!droppedItems.isEmpty()) {
                    items.addAll(droppedItems); // Trigger refreshGrid only once
                    success = true;
                }
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

    public void setViewMode(ViewMode mode) {
        this.currentMode = mode;
        listView.setVisible(mode == ViewMode.LIST);
        gridScroll.setVisible(mode == ViewMode.GRID);
    }

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