package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.util.PdfThumbnailUtil;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * FileGridCell is a custom ListCell implementation used for displaying PDF files
 * in a tile/grid format. It displays a thumbnail preview of the document 
 * alongside its filename.
 */
public class FileGridCell extends ListCell<FileItem> {

    private final ImageView imageView = new ImageView();
    private final VBox container = new VBox(5);
    private final Label nameLabel = new Label();

    /**
     * Constructs a new FileGridCell with a fixed thumbnail size and centered layout.
     */
    public FileGridCell() {
        // Set fixed dimensions for the PDF thumbnail preview
        imageView.setFitWidth(120);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        // Configure the vertical layout container
        container.getChildren().addAll(imageView, nameLabel);
        container.setStyle("-fx-alignment: center;");
    }

    /**
     * Updates the cell's content whenever the underlying FileItem changes.
     * * @param item  The FileItem representing the PDF document.
     * @param empty Whether the cell is currently empty.
     */
    @Override
    protected void updateItem(FileItem item, boolean empty) {
        super.updateItem(item, empty);

        // Reset the cell if it's empty or the item is null
        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        // Set the display name of the file
        nameLabel.setText(item.getName());

        /*
         * Asynchronously load the PDF thumbnail to keep the UI thread responsive.
         * PdfThumbnailUtil handles caching and background rendering.
         */
        PdfThumbnailUtil.loadThumbnailAsync(
                item.getPath(),
                imageView::setImage
        );

        // Set the populated VBox as the visual graphic for the cell
        setGraphic(container);
    }
}