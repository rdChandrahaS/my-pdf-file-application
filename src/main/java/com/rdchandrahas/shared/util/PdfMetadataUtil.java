package com.rdchandrahas.shared.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import java.io.File;

/**
 * PdfMetadataUtil provides helper methods to extract structural and descriptive 
 * information from PDF documents.
 */
public class PdfMetadataUtil {

    /**
     * Opens a PDF file to retrieve its total page count.
     * * @param path The absolute path to the PDF file.
     * @return The number of pages in the document, or 0 if the file is 
     * unreadable or encrypted.
     */
    public static int getPageCount(String path) {
        /*
         * We use a try-with-resources block to ensure the PDDocument 
         * is closed immediately after reading the metadata, 
         * preventing memory leaks and file locks.
         */
        try (PDDocument doc = PDDocument.load(new File(path))) {
            return doc.getNumberOfPages();
        } catch (Exception e) {
            // Log or handle extraction errors (e.g., password-protected files)
            return 0;
        }
    }
}