package com.rdchandrahas.core;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.io.MemoryUsageSetting;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PdfService provides concrete implementations for PDF manipulation tasks.
 * It uses a circular batching strategy to process massive datasets (e.g., 10,000+ files)
 * while respecting UI-defined memory limits and OS file handle constraints.
 */
public class PdfService implements PdfProcessor {

    private static final Logger LOGGER = Logger.getLogger(PdfService.class.getName());
    
    // Controlled by MainController via the UI dropdown
    private static long memoryLimitBytes = 1024L * 1024L * 1024L; // Default 1GB
    
    // Safety limit to prevent "Too many open files" OS errors
    private static final int MAX_OPEN_FILES_BATCH = 500;

    public static void setMemoryLimit(long bytes) {
        memoryLimitBytes = bytes;
        LOGGER.log(Level.INFO, "Global memory limit updated to: {0} bytes", bytes);
    }

    public static MemoryUsageSetting getGlobalMemorySetting() {
        return (memoryLimitBytes == -1) ? 
            MemoryUsageSetting.setupMainMemoryOnly() : 
            MemoryUsageSetting.setupMixed(memoryLimitBytes);
    }

    @Override
    public void merge(List<String> inputFiles, String outputFile) throws Exception {
        List<String> tempFilePaths = new ArrayList<>();
        List<String> currentChunk = new ArrayList<>();
        
        long currentChunkSizeBytes = 0;
        int batchCount = 1;

        LOGGER.log(Level.INFO, "Initiating mass merge for {0} files.", inputFiles.size());

        for (int i = 0; i < inputFiles.size(); i++) {
            File f = new File(inputFiles.get(i));
            if (!f.exists()) continue;

            long fileSize = f.length();

            // Circular Queue Batching Logic:
            // Process the current batch if adding the next file would exceed the RAM limit 
            // OR if we hit the OS open-file handle limit.
            boolean limitExceeded = (memoryLimitBytes != -1 && (currentChunkSizeBytes + fileSize) > memoryLimitBytes);
            boolean handleLimitReached = (currentChunk.size() >= MAX_OPEN_FILES_BATCH);

            if (!currentChunk.isEmpty() && (limitExceeded || handleLimitReached)) {
                String reason = limitExceeded ? "RAM limit" : "File handle limit";
                LOGGER.log(Level.INFO, "Batch {0} full ({1}). Merging to temp storage.", new Object[]{batchCount, reason});
                
                File tempPdf = File.createTempFile("merge_batch_" + batchCount + "_", ".pdf");
                tempPdf.deleteOnExit();
                
                executeMergeInternal(currentChunk, tempPdf.getAbsolutePath());
                tempFilePaths.add(tempPdf.getAbsolutePath());
                
                // Reset queue for the next batch
                currentChunk.clear();
                currentChunkSizeBytes = 0;
                batchCount++;
            }

            currentChunk.add(f.getAbsolutePath());
            currentChunkSizeBytes += fileSize;
        }

        // Handle the final batch
        if (!currentChunk.isEmpty()) {
            if (tempFilePaths.isEmpty()) {
                // Efficiency: If everything fit in one batch, save directly
                executeMergeInternal(currentChunk, outputFile);
                LOGGER.log(Level.INFO, "Merge completed in a single batch.");
                return;
            } else {
                File tempPdf = File.createTempFile("merge_batch_final_", ".pdf");
                tempPdf.deleteOnExit();
                executeMergeInternal(currentChunk, tempPdf.getAbsolutePath());
                tempFilePaths.add(tempPdf.getAbsolutePath());
            }
        }

        // Final step: Combine all temporary batch files into the destination
        LOGGER.log(Level.INFO, "Combining {0} temporary batches into final file: {1}", new Object[]{tempFilePaths.size(), outputFile});
        executeMergeInternal(tempFilePaths, outputFile);
        
        // Cleanup temporary disk space
        for (String tempPath : tempFilePaths) {
            new File(tempPath).delete();
        }
        
        LOGGER.info("Massive merge operation successful.");
    }

    private void executeMergeInternal(List<String> filesToMerge, String outputPath) throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputPath);
        
        for (String path : filesToMerge) {
            merger.addSource(new File(path));
        }
        
        // Use the mixed memory setting to protect the JVM during the final write
        merger.mergeDocuments(getGlobalMemorySetting());
    }
}