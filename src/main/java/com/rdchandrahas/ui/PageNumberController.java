package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PageNumberController extends BaseToolController {

    private static final Logger LOGGER = Logger.getLogger(PageNumberController.class.getName());
    
    // SonarQube Fixes: Extracted magic strings
    private static final String LANG_BENGALI = "Bengali";
    private static final String LANG_HINDI = "Hindi";
    private static final String LANG_FRENCH = "French";
    private static final String LANG_SPANISH = "Spanish";
    private static final String FONT_STANDARD = "Standard (Helvetica)";

    private ComboBox<String> positionCombo;
    private ComboBox<String> styleCombo;
    private ComboBox<String> langCombo;
    private ComboBox<String> fontCombo;
    private ComboBox<String> sizeCombo;
    private ColorPicker colorPicker;

    @Override
    protected void onInitialize() {
        setTitle("Add Page Numbers");
        setActionText("Apply Numbers");

        positionCombo = new ComboBox<>();
        positionCombo.getItems().addAll("Bottom Center", "Bottom Right", "Bottom Left", "Top Center", "Top Right", "Top Left");
        positionCombo.getSelectionModel().selectFirst();

        styleCombo = new ComboBox<>();
        styleCombo.getItems().addAll("1, 2, 3...", "Page 1", "Page 1 of X");
        styleCombo.getSelectionModel().selectFirst();

        langCombo = new ComboBox<>();
        langCombo.getItems().addAll("English", LANG_BENGALI, LANG_HINDI, LANG_FRENCH, LANG_SPANISH);
        langCombo.getSelectionModel().selectFirst();

        sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll("10", "12", "14", "16", "18", "24");
        sizeCombo.getSelectionModel().select("12");
        sizeCombo.setEditable(true);

        fontCombo = new ComboBox<>();
        fontCombo.getItems().add(FONT_STANDARD);
        loadAvailableFonts();
        fontCombo.getSelectionModel().selectFirst();

        colorPicker = new ColorPicker(javafx.scene.paint.Color.BLACK);

        HBox row1 = new HBox(10, new Label("Position:"), positionCombo, new Label("Style:"), styleCombo);
        HBox row2 = new HBox(10, new Label("Language:"), langCombo, new Label("Font:"), fontCombo, new Label("Size:"), sizeCombo, colorPicker);

        addToolbarItem(new VBox(10, row1, row2));
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        String lang = langCombo.getValue();
        if ((lang.equals(LANG_BENGALI) || lang.equals(LANG_HINDI)) && fontCombo.getValue().equals(FONT_STANDARD)) {
            showAlert(Alert.AlertType.WARNING, "Font Required", "Please select a custom .ttf font to display Bengali or Hindi.");
            return;
        }

        processWithSaveDialog("Save PDF", "numbered_document.pdf", (destination) -> {
            List<String> filePaths = fileListView.getItems().stream()
                    .map(FileItem::getPath).collect(Collectors.toList());

            // FIX: Use Temp file to avoid file lock corruption
            File tempMerged = null;
            try {
                String sourcePath;
                if (filePaths.size() > 1) {
                    tempMerged = File.createTempFile("pagenum_merged_", ".pdf");
                    mergeDocumentsSafe(filePaths, tempMerged);
                    sourcePath = tempMerged.getAbsolutePath();
                } else {
                    sourcePath = filePaths.get(0);
                }

                processPdfSafely(new File(sourcePath), destination, (doc) -> {
                    PDFont font = loadSelectedFont(doc);
                    int totalPages = doc.getNumberOfPages();
                    float fontSize = Float.parseFloat(sizeCombo.getValue());
                    javafx.scene.paint.Color fxColor = colorPicker.getValue();

                    for (int i = 0; i < totalPages; i++) {
                        PDPage page = doc.getPage(i);
                        String text = formatPageText(i + 1, totalPages, styleCombo.getValue(), langCombo.getValue());

                        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                            cs.beginText();
                            cs.setFont(font, fontSize);
                            cs.setNonStrokingColor((float) fxColor.getRed(), (float) fxColor.getGreen(), (float) fxColor.getBlue());

                            PDRectangle mediabox = page.getMediaBox();
                            float textWidth = font.getStringWidth(text) / 1000 * fontSize;
                            float x = calculateX(positionCombo.getValue(), mediabox, textWidth);
                            float y = calculateY(positionCombo.getValue(), mediabox);

                            cs.newLineAtOffset(x, y);
                            cs.showText(text);
                            cs.endText();
                        }
                    }
                });
            } finally {
                if (tempMerged != null && tempMerged.exists() && !tempMerged.delete()) {
                    LOGGER.log(Level.WARNING, "Failed to delete temporary file: {0}", tempMerged.getAbsolutePath());
                }
            }
        });
    }

    private PDFont loadSelectedFont(org.apache.pdfbox.pdmodel.PDDocument doc) throws IOException {
        String selectedName = fontCombo.getValue();
        if (selectedName.equals(FONT_STANDARD)) {
            return PDType1Font.HELVETICA;
        }

        File fontFile = new File("fonts", selectedName + ".ttf");
        if (!fontFile.exists()) return PDType1Font.HELVETICA;

        return PDType0Font.load(doc, fontFile);
    }

    private void loadAvailableFonts() {
        File fontDir = new File("fonts");
        if (!fontDir.exists()) fontDir.mkdirs();
        File[] files = fontDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ttf"));
        if (files != null) {
            for (File file : files) {
                fontCombo.getItems().add(file.getName().replace(".ttf", ""));
            }
        }
    }

    private String formatPageText(int current, int total, String style, String lang) {
        String pageWord = switch (lang) {
            case LANG_BENGALI -> "পৃষ্ঠা";
            case LANG_HINDI -> "पृष्ठ";
            case LANG_FRENCH -> "Page";
            case LANG_SPANISH -> "Página";
            default -> "Page";
        };
        String ofWord = switch (lang) {
            case LANG_BENGALI -> "এর";
            case LANG_HINDI -> "का";
            case LANG_FRENCH -> "sur";
            case LANG_SPANISH -> "de";
            default -> "of";
        };

        return switch (style) {
            case "Page 1" -> pageWord + " " + current;
            case "Page 1 of X" -> pageWord + " " + current + " " + ofWord + " " + total;
            default -> String.valueOf(current);
        };
    }

    private float calculateX(String pos, PDRectangle box, float textWidth) {
        float margin = 30;
        if (pos.contains("Left")) return margin;
        if (pos.contains("Right")) return box.getWidth() - margin - textWidth;
        return (box.getWidth() - textWidth) / 2;
    }

    private float calculateY(String pos, PDRectangle box) {
        float margin = 30;
        if (pos.contains("Top")) return box.getHeight() - margin;
        return margin;
    }

    @Override
    protected boolean isInputValid() {
        if (fileListView.getItems().isEmpty()) return false;
        for (FileItem item : fileListView.getItems()) {
            if (!item.getPath().toLowerCase().endsWith(".pdf")) return false;
        }
        return true;
    }
}