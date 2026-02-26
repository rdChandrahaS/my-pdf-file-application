package com.rdchandrahas.core;

import com.rdchandrahas.shared.util.TempFileManager;

import java.awt.image.BufferedImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class ImageToPdfService {

    private static final Logger LOGGER = Logger.getLogger(ImageToPdfService.class.getName());

    // FIX 1: Throw specific IOException instead of generic Exception
    public void convertImagesToPdf(List<String> imagePaths, String outputPath) throws IOException {
        LOGGER.log(Level.INFO, "Starting PARALLEL Image to PDF conversion for {0} images.", imagePaths.size());

        List<String> tempPdfPaths = imagePaths.parallelStream().map(imagePath -> {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) return null;

            try {
                BufferedImage bImage = ImageIO.read(imageFile);
                if (bImage == null) {
                    LOGGER.log(Level.WARNING, "Failed to decode image: {0}", imageFile.getName());
                    return null;
                }

                try (PDDocument tempDoc = new PDDocument(PdfService.getGlobalMemorySetting())) {
                    
                    PDImageXObject pdImage = JPEGFactory.createFromImage(tempDoc, bImage, 0.85f);
                    bImage.flush(); 
                    
                    PDRectangle pageSize = new PDRectangle(pdImage.getWidth(), pdImage.getHeight());
                    PDPage page = new PDPage(pageSize);
                    tempDoc.addPage(page);
                    
                    try (PDPageContentStream contentStream = new PDPageContentStream(tempDoc, page)) {
                        contentStream.drawImage(pdImage, 0, 0);
                    }
                    
                    // FIX 2: Use TempFileManager
                    File tempPdf = TempFileManager.createTempFile("parallel_img_", ".pdf");
                    tempDoc.save(tempPdf.getAbsolutePath());
                    
                    return tempPdf.getAbsolutePath();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing image: " + imagePath, e);
                return null;
            }
        })
        .filter(Objects::nonNull) 
        .collect(Collectors.toCollection(ArrayList::new));

        if (!tempPdfPaths.isEmpty()) {
            LOGGER.log(Level.INFO, "Merging {0} parallel-processed pages into final PDF...", tempPdfPaths.size());
            
            // FIX 3: Guaranteed Cleanup using try-finally
            try {
                PdfService pdfService = new PdfService();
                // Note: We catch the GeneralSecurityException from merge and wrap it in an IOException
                try {
                    pdfService.merge(tempPdfPaths, outputPath);
                } catch (java.security.GeneralSecurityException e) {
                    throw new IOException("Failed to merge PDFs due to security exception", e);
                }
                LOGGER.log(Level.INFO, "Parallel conversion and merge complete!");
            } finally {
                // Clean up the temporary hard drive files even if the merge fails
                for (String path : tempPdfPaths) {
                    File tempFile = new File(path);
                    if (tempFile.exists() && !tempFile.delete()) {
                        LOGGER.log(Level.WARNING, "Failed to delete temp file: {0}", path);
                    }
                }
            }
        }
    }
}