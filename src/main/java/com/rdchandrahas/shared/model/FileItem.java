package com.rdchandrahas.shared.model;

import java.io.File;

/**
 * FileItem represents a wrapper for a physical file on the system.
 * It caches metadata such as the path, name, and size to avoid redundant 
 * I/O operations and provides a placeholder for PDF-specific data like page counts.
 */
public class FileItem {

    private final String path;
    private final String name;
    private final long size;
    private int pageCount;
    private final File file;

    /**
     * Constructs a FileItem from a system path.
     * * @param path The absolute path to the file.
     */
    public FileItem(String path) {
        this.file = new File(path);

        this.path = path;
        this.name = file.getName();
        this.size = file.length();
        this.pageCount = -1; // Unknown initially; populated by PdfMetadataUtil if needed
    }

    /** @return The absolute file system path. */
    public String getPath() {
        return path;
    }

    /** @return The simple name of the file (including extension). */
    public String getName() {
        return name;
    }

    /** @return The file size in bytes. */
    public long getSize() {
        return size;
    }

    /** * @return The number of pages in the document. 
     * Returns -1 if the count hasn't been calculated or is not applicable.
     */
    public int getPageCount() {
        return pageCount;
    }

    /** * Updates the page count after metadata extraction.
     * * @param pageCount The number of pages found in the PDF.
     */
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    /**
     * Standard equality check based on the unique file path.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FileItem other)) return false;
        return path.equals(other.path);
    }

    /**
     * Standard hash code generation based on the unique file path.
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }
    
    /**
     * Returns the underlying Java IO File object.
     * * @return The file instance.
     */
    public File getFile() {
        return this.file;
    }
}