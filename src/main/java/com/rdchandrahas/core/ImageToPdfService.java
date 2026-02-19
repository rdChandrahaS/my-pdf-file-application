package com.rdchandrahas.core;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageToPdfService {

    private static final Logger LOGGER = Logger.getLogger(ImageToPdfService.class.getName());
    // Safe batch limit: Flush RAM to Hard Disk every 100 images
    private static final int IMAGE_BATCH_SIZE = 100; 

    public void convertImagesToPdf(List<String> imagePaths, String outputPath) throws Exception {
        LOGGER.log(Level.INFO, "Starting Image to PDF conversion for {0} images.", imagePaths.size());

        List<String> tempPdfPaths = new ArrayList<>();
        PDDocument currentDoc = new PDDocument(PdfService.getGlobalMemorySetting());
        int currentBatchCount = 0;
        int batchIndex = 1;

        try {
            for (int i = 0; i < imagePaths.size(); i++) {
                File imageFile = new File(imagePaths.get(i));
                if (!imageFile.exists()) continue;

                PDImageXObject pdImage = PDImageXObject.createFromFile(imageFile.getAbsolutePath(), currentDoc);
                PDRectangle pageSize = new PDRectangle(pdImage.getWidth(), pdImage.getHeight());
                PDPage page = new PDPage(pageSize);
                currentDoc.addPage(page);
                
                try (PDPageContentStream contentStream = new PDPageContentStream(currentDoc, page)) {
                    contentStream.drawImage(pdImage, 0, 0);
                }

                currentBatchCount++;

                // CIRCULAR QUEUE LOGIC: Shift to Hard Disk when batch is full
                if (currentBatchCount >= IMAGE_BATCH_SIZE) {
                    File tempPdf = File.createTempFile("img_batch_" + batchIndex + "_", ".pdf");
                    tempPdf.deleteOnExit();
                    
                    currentDoc.save(tempPdf.getAbsolutePath());
                    currentDoc.close(); // INSTANTLY FREE RAM
                    tempPdfPaths.add(tempPdf.getAbsolutePath());
                    
                    LOGGER.log(Level.INFO, "Saved image batch {0} to temp storage.", batchIndex);
                    
                    currentDoc = new PDDocument(PdfService.getGlobalMemorySetting()); // Start fresh
                    currentBatchCount = 0;
                    batchIndex++;
                }
            }

            // Flush remaining images
            if (currentBatchCount > 0) {
                if (tempPdfPaths.isEmpty()) {
                    // No temp files needed, save directly
                    currentDoc.save(outputPath);
                    currentDoc.close();
                    currentDoc = null; 
                    return; 
                } else {
                    File tempPdf = File.createTempFile("img_batch_final_", ".pdf");
                    tempPdf.deleteOnExit();
                    currentDoc.save(tempPdf.getAbsolutePath());
                    currentDoc.close();
                    currentDoc = null;
                    tempPdfPaths.add(tempPdf.getAbsolutePath());
                }
            }
        } finally {
            if (currentDoc != null) currentDoc.close(); 
        }

        // Merge all temp files securely using your PdfService
        if (!tempPdfPaths.isEmpty()) {
            LOGGER.log(Level.INFO, "Merging {0} temp batches into final PDF...", tempPdfPaths.size());
            PdfService pdfService = new PdfService();
            pdfService.merge(tempPdfPaths, outputPath);
            
            for (String path : tempPdfPaths) {
                new File(path).delete(); // Cleanup hard disk
            }
        }
    }
}