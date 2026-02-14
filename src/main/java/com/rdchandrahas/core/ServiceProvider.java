package com.rdchandrahas.core;

/**
 * Acts as a simple Dependency Injection (DI) Container to provide shared service instances 
 * across the application. This ensures that heavy service objects are instantiated only once 
 * and can be accessed globally.
 */
public class ServiceProvider {
    
    /** * The shared instance of the PDF processing engine. 
     * Initialized with PdfService which utilizes Apache PDFBox.
     */
    private static final PdfProcessor pdfProcessor = new PdfService(); 

    /**
     * Retrieves the global singleton instance of the PdfProcessor.
     * * @return The active PdfProcessor implementation.
     */
    public static PdfProcessor getPdfProcessor() {
        return pdfProcessor;
    }
}