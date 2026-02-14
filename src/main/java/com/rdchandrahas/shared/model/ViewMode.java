package com.rdchandrahas.shared.model;

/**
 * ViewMode defines the available display layouts for the file explorer components.
 * This enum is used by the FileListView to toggle between a metadata-rich list 
 * and a visual thumbnail-based grid.
 */
public enum ViewMode {
    /** * Represents a vertical list layout with file details (name, size, etc.) 
     * and small preview icons.
     */
    LIST,

    /** * Represents a tile-based grid layout focused on large visual 
     * thumbnail previews of the PDF documents.
     */
    GRID
}