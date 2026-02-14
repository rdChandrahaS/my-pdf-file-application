package com.rdchandrahas.shared.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * TempFileManager handles the creation and lifecycle of temporary files used during 
 * PDF processing. It ensures that all intermediate data is stored in a dedicated 
 * directory and automatically cleaned up when the application exits.
 */
public class TempFileManager {

    /** The path to the session-specific temporary directory. */
    private static Path appTempDir;

    /**
     * Retrieves or creates a dedicated temporary directory for the current application run.
     * Registers a JVM shutdown hook to ensure the directory is deleted upon exit.
     * * @return The Path to the application's temporary directory.
     * @throws IOException If the directory cannot be created.
     */
    public static synchronized Path getTempDir() throws IOException {
        if (appTempDir == null) {
            // Create a uniquely named temp folder with the prefix "PDFGear_"
            appTempDir = Files.createTempDirectory("PDFGear_");
            
            // Standard practice: Ensure it gets deleted when the JVM shuts down
            Runtime.getRuntime().addShutdownHook(new Thread(() -> cleanup(appTempDir)));
        }
        return appTempDir;
    }

    /**
     * Generates a new temporary file within the application's dedicated temp folder.
     * * @param prefix The starting name of the file (e.g., "merge_").
     * @param suffix The file extension (e.g., ".pdf").
     * @return A File object pointing to the newly created temp file.
     * @throws IOException If the file cannot be created.
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(getTempDir(), prefix, suffix).toFile();
    }

    /**
     * Recursively deletes a directory and all its contents.
     * Uses a reverse-order sort to ensure files are deleted before their parent folders.
     * * @param path The Path to the directory or file to be removed.
     */
    public static void cleanup(Path path) {
        if (path == null || !Files.exists(path)) return;

        try {
            // Traverse the file tree
            Files.walk(path)
                .sorted(Comparator.reverseOrder()) // Ensure children are deleted before parents
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            // Silently log or print error; critical cleanup shouldn't crash the shutdown process
            System.err.println("Could not clean up temp files: " + e.getMessage());
        }
    }
}