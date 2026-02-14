package com.rdchandrahas.core;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.io.MemoryUsageSetting;
import java.io.File;
import java.util.List;

/**
 * PdfService provides concrete implementations for PDF manipulation tasks using Apache PDFBox.
 * It manages memory usage strategies dynamically based on file sizes and user-defined limits,
 * ensuring stability when processing large documents.
 */
public class PdfService implements PdfProcessor {

    /** Default limit: 1 GB (stored in bytes). A value of -1 indicates Unrestricted RAM usage. */
    private static long memoryLimitBytes = 1024L * 1024L * 1024L;

    /**
     * Updates the global memory limit for PDF processing operations.
     * * @param bytes Limit in bytes, or -1 for unrestricted RAM usage.
     */
    public static void setMemoryLimit(long bytes) {
        memoryLimitBytes = bytes;
        System.out.println("Memory limit updated to: " + (bytes == -1 ? "Unrestricted" : bytes + " bytes"));
    }

    /**
     * Merges multiple PDF files into a single destination file.
     * Chooses between RAM-only and Disk-based processing based on the configured memory limit.
     * * @param inputFiles List of source file paths.
     * @param outputFile Path for the resulting merged PDF.
     * @throws Exception if an I/O error or PDF processing error occurs.
     */
    @Override
    public void merge(List<String> inputFiles, String outputFile) throws Exception {
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputFile);

        long totalInputSize = 0;
        
        // Add source files to the utility and calculate the aggregate size for memory strategy selection
        for (String path : inputFiles) {
            File f = new File(path);
            merger.addSource(f);
            if (f.exists()) {
                totalInputSize += f.length();
            }
        }

        MemoryUsageSetting settings;

        // Logic: Use Main Memory (RAM) if unrestricted or if total size is within the defined limit.
        // Otherwise, use temporary files on Disk to prevent OutOfMemoryErrors.
        if (memoryLimitBytes == -1 || totalInputSize < memoryLimitBytes) {
            System.out.println("Processing in RAM. (Total Size: " + formatSize(totalInputSize) + ")");
            settings = MemoryUsageSetting.setupMainMemoryOnly();
        } else {
            System.out.println("Processing on Disk/Storage. (Total Size: " + formatSize(totalInputSize) + " exceeds limit)");
            settings = MemoryUsageSetting.setupTempFileOnly();
        }

        merger.mergeDocuments(settings);
    }
    
    /**
     * Internal helper to convert byte counts into human-readable strings (e.g., "10.5 MB").
     * * @param v Size in bytes.
     * @return Formatted size string.
     */
    private String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}