package com.rdchandrahas.shared.util;

import com.rdchandrahas.shared.model.FileItem;
import java.util.List;

/**
 * DuplicateDetector provides utility methods to ensure that the same file 
 * is not added multiple times to a processing list.
 */
public class DuplicateDetector {

    /**
     * Checks if a specific FileItem already exists within a list based on its absolute path.
     * * @param items   The current list of FileItems in the view.
     * @param newItem The new FileItem attempting to be added.
     * @return true if a file with the same path is already present in the list.
     */
    public static boolean exists(List<FileItem> items, FileItem newItem) {
        /*
         * We compare using paths rather than the Object itself to catch cases 
         * where two different FileItem instances point to the same physical file.
         */
        return items.stream()
                .anyMatch(item ->
                        item.getPath().equals(newItem.getPath()));
    }
}