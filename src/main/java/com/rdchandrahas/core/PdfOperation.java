package com.rdchandrahas.core;

import org.apache.pdfbox.pdmodel.PDDocument;

@FunctionalInterface
public interface PdfOperation {
    void execute(PDDocument document) throws Exception;
}