package com.rdchandrahas.shared.util;

import javafx.scene.image.Image;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ThumbnailCache provides an in-memory storage solution for generated thumbnails.
 * It uses a thread-safe ConcurrentHashMap and implements a simple memory-limit 
 * enforcement strategy to prevent the application from consuming excessive RAM.
 */
public class ThumbnailCache {

    private ThumbnailCache() {
        throw new IllegalStateException("Utility class");
    }
    
    /** Thread-safe storage for images, keyed by their absolute file system path. */
    private static final Map<String, Image> cache = new ConcurrentHashMap<>();

    /** Default memory limit: 500 MB (expressed in bytes). */
    private static long maxSizeBytes = 500L * 1024L * 1024L;
    
    /** Tracking variable for the estimated current memory footprint of the cache. */
    private static long currentSizeBytes = 0;

    /**
     * Updates the maximum allowed cache size at runtime.
     * * @param bytes The new limit in bytes.
     */
    public static void setMaxSizeBytes(long bytes) {
        maxSizeBytes = bytes;
        System.out.println("Cache limit updated to: " + (bytes / (1024 * 1024)) + " MB");
        enforceLimit();
    }

    /**
     * Retrieves an image from the cache.
     * * @param path The absolute path of the file used as the key.
     * @return The cached Image, or null if not found.
     */
    public static Image get(String path) {
        return cache.get(path);
    }

    /**
     * Adds an image to the cache and estimates its memory usage.
     * * @param path  The absolute path of the file.
     * @param image The JavaFX Image object to store.
     */
    public static void put(String path, Image image) {
        if (image == null) return;

        /* * Estimate memory size: Width * Height * 4 bytes.
         * This assumes a standard 32-bit (ARGB) color depth per pixel.
         */
        long imgSize = (long) (image.getWidth() * image.getHeight() * 4);

        // Safety check: If a single image is larger than the entire limit, do not cache it.
        if (imgSize > maxSizeBytes) return;

        // Add to cache and update usage tracker
        cache.put(path, image);
        currentSizeBytes += imgSize;

        // Check if the new total exceeds the defined memory threshold
        enforceLimit();
    }

    /**
     * Checks if a thumbnail for the given path is already cached.
     * * @param path The absolute path to check.
     * @return true if the image is in the cache.
     */
    public static boolean contains(String path) {
        return cache.containsKey(path);
    }

    /**
     * Clears the entire cache and resets the memory usage counter.
     */
    public static void clear() {
        cache.clear();
        currentSizeBytes = 0;
        System.out.println("Cache cleared to free system memory.");
    }

    /**
     * Internal logic to prevent memory overflow. 
     * If the current usage exceeds the limit, the cache is flushed.
     * * SUGGESTION: For a more advanced implementation, consider using a 
     * LinkedHashMap with access-order to implement a Least Recently Used (LRU) 
     * eviction policy instead of a full flush.
     */
    private static void enforceLimit() {
        if (currentSizeBytes > maxSizeBytes) {
            clear();
        }
    }
}