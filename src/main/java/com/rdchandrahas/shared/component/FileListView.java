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
 * a traditional list view or a visual tile/grid view. It includes built-in 
 * support for drag-and-drop file imports from the desktop and internal reordering.
 */
public class FileListView extends StackPane {

    private final ObservableList<FileItem> items = FXCollections.observableArrayList();
    private final ListView<FileItem> listView = new ListView<>(items);
    private final TilePane gridPane = new TilePane();
    private final ScrollPane gridScroll = new ScrollPane(gridPane);
    private ViewMode currentMode = ViewMode.LIST;

    /**
     * Constructs the FileListView, initializing both List and Grid layouts 
     * and enabling OS-level drag-and-drop support.
     */
    public FileListView() {
        // --- Grid Layout Configuration ---
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setPrefTileWidth(180);
        gridPane.setPrefTileHeight(240);
        gridPane.setAlignment(Pos.TOP_LEFT);
        
        // CSS class handles background and padding
        gridPane.getStyleClass().add("grid-pane-container");

        gridScroll.setFitToWidth(true);
        gridScroll.setPannable(true);
        gridScroll.getStyleClass().add("grid-scroll");

        // Both views occupy the same space; visibility is toggled by ViewMode
        getChildren().addAll(listView, gridScroll);

        setupListView();
        setupGridView();
        setupDesktopDropSupport();
        
        // Default startup mode
        setViewMode(ViewMode.LIST);
    }

    /**
     * Configures the ListView cell factory for the standard list metadata display.
     */
    private void setupListView() {
        listView.setCellFactory(lv -> new FileListCell());
    }

    /**
     * Sets up a listener to automatically refresh the Grid display whenever 
     * the underlying item list is modified.
     */
    private void setupGridView() {
        items.addListener((javafx.collections.ListChangeListener<FileItem>) c -> refreshGrid());
        refreshGrid();
    }

    /**
     * Clears and repopulates the TilePane with visual cards for each file item.
     */
    private void refreshGrid() {
        gridPane.getChildren().clear();
        for (int i = 0; i < items.size(); i++) {
            FileItem item = items.get(i);
            VBox card = createGridCard(item, i);
            gridPane.getChildren().add(card);
        }
    }

    /**
     * Creates a visual "Card" for a file, containing a large thumbnail and the filename.
     * * @param item  The file data.
     * @param index The current position in the list (used for reordering).
     * @return A VBox representing the file card.
     */
    private VBox createGridCard(FileItem item, int index) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(170);
        
        // Theming handled via CSS
        card.getStyleClass().add("grid-card");

        ImageView image = new ImageView();
        image.setFitWidth(150);
        image.setFitHeight(200);
        
        // Ensures the PDF preview maintains its original shape
        image.setPreserveRatio(true); 

        Label name = new Label(item.getName());
        name.setWrapText(true);
        name.setMaxWidth(160);
        name.setAlignment(Pos.CENTER);

        // Load the preview image from the background utility
        PdfThumbnailUtil.loadThumbnailAsync(item.getPath(), image::setImage);
        
        card.getChildren().addAll(image, name);
        
        // Enable drag-to-reorder within the grid
        enableDragReorderGrid(card, index);
        
        return card;
    }

    /**
     * Configures drag-and-drop listeners to allow users to drag files 
     * from their computer directly into the application.
     */
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
                    // Filter for supported file types
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

    /**
     * Logic to handle reordering files within the Grid view via drag-and-drop.
     * * @param card  The visual card being dragged or dropped onto.
     * @param index The original index of the item.
     */
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
     * Switches the UI between LIST view (metadata rows) and GRID view (visual thumbnails).
     * * @param mode The desired ViewMode.
     */
    public void setViewMode(ViewMode mode) {
        currentMode = mode;
        listView.setVisible(mode == ViewMode.LIST);
        gridScroll.setVisible(mode == ViewMode.GRID);
    }

    /** @return The observable list of file items currently loaded. */
    public ObservableList<FileItem> getItems() { return items; }
    
    /** @return The item currently selected in the ListView. */
    public FileItem getSelectedItem() { return listView.getSelectionModel().getSelectedItem(); }
    
    /**
     * Sorts the internal collection by filename.
     * * @param ascending True for A-Z, False for Z-A.
     */
    public void sortByName(boolean ascending) {
        items.sort((a, b) -> ascending ? a.getName().compareToIgnoreCase(b.getName()) : b.getName().compareToIgnoreCase(a.getName()));
    }

    /**
     * Sorts the internal collection by file size.
     * * @param ascending True for Small-Large, False for Large-Small.
     */
    public void sortBySize(boolean ascending) {
        items.sort((a, b) -> {
            int result = Long.compare(a.getSize(), b.getSize());
            return ascending ? result : -result;
        });
    }
}