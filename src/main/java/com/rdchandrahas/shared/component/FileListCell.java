package com.rdchandrahas.shared.component;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.shared.util.FileUtils;
import com.rdchandrahas.shared.util.PdfThumbnailUtil;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * FileListCell is a custom ListCell implementation used for displaying PDF files
 * in a traditional list format. It displays a small thumbnail on the left, 
 * with the filename and file size stacked vertically on the right.
 */
public class FileListCell extends ListCell<FileItem> {

    private final ImageView imageView = new ImageView();
    private final VBox textBox = new VBox(4);
    private final HBox container = new HBox(10);

    /**
     * Constructs a new FileListCell with a horizontal layout and fixed thumbnail scaling.
     */
    public FileListCell() {
        // Configure the thumbnail size for the list view
        imageView.setFitWidth(60);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);

        // Build the horizontal layout: [Thumbnail] [Text Metadata]
        container.getChildren().addAll(imageView, textBox);
        container.setStyle("-fx-alignment: center-left;");
    }

    /**
     * Updates the cell's visual content based on the provided FileItem.
     * * @param item  The FileItem data for the current row.
     * @param empty Whether the row is empty.
     */
    @Override
    protected void updateItem(FileItem item, boolean empty) {
        super.updateItem(item, empty);

        // Reset the cell if it is empty or has no data
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        // Clear existing labels to prevent duplication during cell reuse
        textBox.getChildren().clear();

        // Create labels for file metadata
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-weight: bold;"); // Visual distinction for the filename
        
        Label sizeLabel = new Label(
                FileUtils.formatSize(item.getSize())
        );
        sizeLabel.getStyleClass().add("secondary-label"); // Assuming a secondary style for metadata

        textBox.getChildren().addAll(nameLabel, sizeLabel);

        // Clear previous image to avoid flickering while the new one loads
        imageView.setImage(null);

        /*
         * Asynchronously load the PDF thumbnail.
         * PdfThumbnailUtil manages the background rendering and image caching.
         */
        PdfThumbnailUtil.loadThumbnailAsync(
                item.getPath(),
                imageView::setImage
        );

        // Display the populated container as the cell's graphic
        setGraphic(container);
    }
}