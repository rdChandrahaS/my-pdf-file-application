package com.rdchandrahas.shared.util;

/**
 * FileUtils provides general-purpose helper methods for handling file-related data.
 * It primarily focuses on formatting raw system data into user-friendly strings.
 */
public class FileUtils {

    /**
     * Converts a raw byte count into a human-readable format (B, KB, MB, or GB).
     * This is used throughout the UI to display file sizes in lists and grids.
     * * @param bytes The size of the file in bytes.
     * @return A formatted string representing the file size (e.g., "1.50 MB").
     */
    public static String formatSize(long bytes) {
        // Handle small files in bytes
        if (bytes < 1024) return bytes + " B";

        // Convert to Kilobytes
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.2f KB", kb);

        // Convert to Megabytes
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.2f MB", mb);

        // Fallback to Gigabytes
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }
}