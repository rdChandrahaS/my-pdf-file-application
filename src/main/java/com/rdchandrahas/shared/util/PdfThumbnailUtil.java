package com.rdchandrahas.shared.util;

import com.rdchandrahas.core.ExecutionManager;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Set;
import java.util.function.Consumer;

/**
 * PdfThumbnailUtil handles the generation and asynchronous loading of file previews.
 * It supports both PDF documents (rendering the first page) and standard image formats,
 * ensuring the UI remains responsive by offloading heavy rendering tasks to background threads.
 */
public class PdfThumbnailUtil {

    private PdfThumbnailUtil() {
        throw new IllegalStateException("Utility class");
    }
    
    /** Set of supported image extensions for direct thumbnail loading. */
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".bmp", ".gif"
    );

    /**
     * Synchronously generates a thumbnail Image for a given file.
     * For images, it loads a scaled version. For PDFs, it renders the first page at 72 DPI.
     *
     * @param file The file to preview.
     * @return A JavaFX Image object, or null if rendering fails.
     */
    public static Image generateThumbnail(File file) {
        String filename = file.getName().toLowerCase();

        // Handle direct image files
        if (isImage(filename)) {
            try (FileInputStream fis = new FileInputStream(file)) {
                // Request a 200px width thumbnail while preserving aspect ratio
                return new Image(fis, 200, 0, true, true);
            } catch (Exception e) {
                return null;
            }
        }

        // Handle PDF document rendering
        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(document);
            // Render the first page (index 0) at standard screen resolution (72 DPI)
            BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 72);
            // Convert Swing BufferedImage to JavaFX Image
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Loads a thumbnail asynchronously using the ExecutionManager.
     * It checks the cache first, generates the image if missing, and updates the UI via Platform.runLater.
     *
     * @param filePath The absolute path of the file.
     * @param callback A consumer function to handle the resulting Image on the UI thread.
     */
    public static void loadThumbnailAsync(String filePath, Consumer<Image> callback) {
        ExecutionManager.submit(() -> {
            File file = new File(filePath);
            
            // Attempt to retrieve from the memory cache using the absolute path as key
            Image thumbnail = ThumbnailCache.get(file.getAbsolutePath());

            if (thumbnail == null) {
                thumbnail = generateThumbnail(file);
                if (thumbnail != null) {
                    // Store the newly generated thumbnail in cache for future use
                    ThumbnailCache.put(file.getAbsolutePath(), thumbnail);
                }
            }

            final Image result = thumbnail;
            // Return the result to the JavaFX Application Thread
            Platform.runLater(() -> callback.accept(result));
        });
    }

    /**
     * Helper to determine if a file is an image based on its extension.
     * * @param filename The name of the file.
     * @return true if the extension matches IMAGE_EXTENSIONS.
     */
    private static boolean isImage(String filename) {
        for (String ext : IMAGE_EXTENSIONS) {
            if (filename.endsWith(ext)) return true;
        }
        return false;
    }
}