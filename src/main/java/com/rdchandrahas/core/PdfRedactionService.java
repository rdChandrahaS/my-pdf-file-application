package com.rdchandrahas.core;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PdfRedactionService {

    private static final Logger LOGGER = Logger.getLogger(PdfRedactionService.class.getName());

    public void redactText(String inputPath, String outputPath, String textToRedact) throws Exception {
        LOGGER.log(Level.INFO, "Starting visual text redaction for {0}", inputPath);

        try (PDDocument document = PDDocument.load(new File(inputPath), PdfService.getGlobalMemorySetting())) {
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);
                List<TextPosition> boundingBoxes = new ArrayList<>();

                PDFTextStripper stripper = new PDFTextStripper() {
                    @Override
                    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                        String lowerText = text.toLowerCase();
                        String lowerTarget = textToRedact.toLowerCase();
                        int index = 0;
                        
                        while ((index = lowerText.indexOf(lowerTarget, index)) != -1) {
                            for (int j = index; j < index + lowerTarget.length(); j++) {
                                if (j < textPositions.size()) {
                                    boundingBoxes.add(textPositions.get(j));
                                }
                            }
                            index += lowerTarget.length();
                        }
                        super.writeString(text, textPositions);
                    }
                };

                stripper.setSortByPosition(true);
                stripper.setStartPage(i + 1);
                stripper.setEndPage(i + 1);
                
                // Write to a dummy stream to trigger the processing and collection of TextPositions
                stripper.writeText(document, new OutputStreamWriter(new ByteArrayOutputStream()));

                if (!boundingBoxes.isEmpty()) {
                    try (PDPageContentStream contentStream = new PDPageContentStream(
                            document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                        
                        contentStream.setNonStrokingColor(Color.BLACK);
                        for (TextPosition pos : boundingBoxes) {
                            float x = pos.getXDirAdj();
                            // Convert TextPosition Y (from top) to PDFBox coordinate system (from bottom)
                            float y = page.getMediaBox().getHeight() - pos.getYDirAdj();
                            float width = pos.getWidthDirAdj();
                            float height = pos.getHeightDir();

                            // Slight adjustments to perfectly cover the text area
                            contentStream.addRect(x, y - (height * 0.2f), width, height * 1.2f);
                            contentStream.fill();
                        }
                    }
                }
            }
            document.save(outputPath);
        }
    }
}