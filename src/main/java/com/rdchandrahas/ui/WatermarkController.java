package com.rdchandrahas.ui;

import com.rdchandrahas.shared.model.FileItem;
import com.rdchandrahas.ui.base.BaseToolController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WatermarkController extends BaseToolController {

    private static final Logger LOGGER = Logger.getLogger(WatermarkController.class.getName());
    
    // Constants for SonarQube compliance
    private static final String FONT_HELVETICA = "Helvetica";
    private static final String FONT_TIMES = "Times Roman";
    private static final String FONT_COURIER = "Courier";
    private static final String FONT_MANUAL = "Manual Select (.ttf)...";

    private TextField watermarkInput;
    private ColorPicker colorPicker;
    private ComboBox<String> fontCombo;
    private CheckBox boldCheck, italicCheck;
    private ComboBox<String> sizeCombo;
    private Slider rotateSlider, opacitySlider;
    private Button previewBtn;
    private File manualCustomFile = null;

    @Override
    protected void onInitialize() {
        setTitle("Watermark PDF");
        setActionText("Apply Watermark");
        
        watermarkInput = new TextField("CONFIDENTIAL");
        watermarkInput.textProperty().addListener((o, old, n) -> updateActionBtnState());
        
        colorPicker = new ColorPicker(Color.DARKGRAY);
        
        fontCombo = new ComboBox<>();
        fontCombo.getItems().addAll(FONT_HELVETICA, FONT_TIMES, FONT_COURIER, FONT_MANUAL);
        fontCombo.getSelectionModel().selectFirst();
        
        // FIX: Add listener to open file chooser when Manual is selected
        fontCombo.setOnAction(e -> {
            if (FONT_MANUAL.equals(fontCombo.getValue())) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Select TTF Font");
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TrueType Fonts", "*.ttf"));
                File selected = fc.showOpenDialog(actionBtn.getScene().getWindow());
                if (selected != null) {
                    manualCustomFile = selected;
                } else {
                    fontCombo.getSelectionModel().selectFirst(); // Revert if cancelled
                }
            } else {
                manualCustomFile = null; // Reset if they switch back to standard fonts
            }
        });
        
        boldCheck = new CheckBox("Bold");
        italicCheck = new CheckBox("Italic");
        
        sizeCombo = new ComboBox<>();
        sizeCombo.getItems().addAll("24", "48", "72", "120");
        sizeCombo.getSelectionModel().select("72");
        sizeCombo.setEditable(true);
        
        rotateSlider = new Slider(0, 360, 45);
        opacitySlider = new Slider(0.1, 1.0, 0.3);
        
        previewBtn = new Button("Preview");
        previewBtn.setOnAction(e -> showPreview());

        addToolbarItem(new VBox(10, 
            new HBox(10, watermarkInput, colorPicker, fontCombo, boldCheck, italicCheck), 
            new HBox(10, sizeCombo, rotateSlider, opacitySlider, previewBtn)
        ));
    }

    @Override
    protected void handleAddFiles() { 
        addFiles("PDF Files", "*.pdf"); 
    }

    @Override
    protected void handleAction() {
        WatermarkConfig config = getCurrentConfig();
        processWithSaveDialog("Save Watermarked PDF", "watermarked.pdf", (destination) -> {
            List<String> paths = fileListView.getItems().stream().map(FileItem::getPath).collect(Collectors.toList());
            
            // FIX: Write merge to a Temporary File first to avoid Read/Write conflict on the same path
            File tempMerged = File.createTempFile("watermark_merged_", ".pdf");
            tempMerged.deleteOnExit();
            
            try {
                mergeDocumentsSafe(paths, tempMerged);
                
                // Now read from temp file, and save to final destination
                processPdfSafely(tempMerged, destination, (doc) -> {
                    PDFont font = loadSelectedFont(doc);
                    for (PDPage page : doc.getPages()) {
                        applyWatermark(doc, page, config, font);
                    }
                });
            } finally {
                // Clean up the temp file
                if (tempMerged.exists() && !tempMerged.delete()) {
                    LOGGER.log(Level.WARNING, "Failed to delete temp file: {0}", tempMerged.getAbsolutePath());
                }
            }
        });
    }

    private void showPreview() {
        WatermarkConfig config = getCurrentConfig();
        setBusy(true, previewBtn);
        new Thread(() -> {
            try (PDDocument doc = createDocumentSafe()) {
                PDPage page = new PDPage(PDRectangle.A4); 
                doc.addPage(page);
                applyWatermark(doc, page, config, loadSelectedFont(doc));
                
                BufferedImage bim = new PDFRenderer(doc).renderImageWithDPI(0, 100, ImageType.RGB);
                Platform.runLater(() -> { 
                    setBusy(false, previewBtn); 
                    displayPreviewDialog(SwingFXUtils.toFXImage(bim, null)); 
                });
            } catch (Exception e) { 
                Platform.runLater(() -> { 
                    setBusy(false, previewBtn); 
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage()); 
                }); 
            }
        }).start();
    }

    private void applyWatermark(PDDocument doc, PDPage page, WatermarkConfig config, PDFont font) throws IOException {
        try (PDPageContentStream cs = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(config.opacity);
            cs.setGraphicsStateParameters(gs);
            cs.setNonStrokingColor(config.color);
            cs.beginText();
            cs.setFont(font, config.fontSize);
            
            PDRectangle box = page.getMediaBox();
            float txW = font.getStringWidth(config.text) / 1000 * config.fontSize;
            
            Matrix m = Matrix.getRotateInstance(Math.toRadians(config.rotation), box.getWidth()/2, box.getHeight()/2);
            m.translate(-txW/2, 0);
            
            cs.setTextMatrix(m);
            cs.showText(config.text);
            cs.endText();
        }
    }

    private PDFont loadSelectedFont(PDDocument doc) throws IOException {
        if (manualCustomFile != null) return PDType0Font.load(doc, manualCustomFile);
        
        String selection = fontCombo.getValue();
        boolean b = boldCheck.isSelected();
        boolean i = italicCheck.isSelected();
        
        // FIX: Completed the missing font logic for all combinations without nested ternaries
        if (FONT_COURIER.equals(selection)) {
            if (b && i) return PDType1Font.COURIER_BOLD_OBLIQUE;
            if (b) return PDType1Font.COURIER_BOLD;
            if (i) return PDType1Font.COURIER_OBLIQUE;
            return PDType1Font.COURIER;
        } 
        else if (FONT_TIMES.equals(selection)) {
            if (b && i) return PDType1Font.TIMES_BOLD_ITALIC;
            if (b) return PDType1Font.TIMES_BOLD;
            if (i) return PDType1Font.TIMES_ITALIC;
            return PDType1Font.TIMES_ROMAN;
        } 
        else { // FONT_HELVETICA or fallback
            if (b && i) return PDType1Font.HELVETICA_BOLD_OBLIQUE;
            if (b) return PDType1Font.HELVETICA_BOLD;
            if (i) return PDType1Font.HELVETICA_OBLIQUE;
            return PDType1Font.HELVETICA;
        }
    }

    private WatermarkConfig getCurrentConfig() {
        Color c = colorPicker.getValue();
        float size = 72; // default
        try {
            size = Float.parseFloat(sizeCombo.getValue());
        } catch (NumberFormatException ignored) {}
        
        return new WatermarkConfig(
            watermarkInput.getText(), 
            size, 
            new java.awt.Color((float)c.getRed(), (float)c.getGreen(), (float)c.getBlue()), 
            (float)rotateSlider.getValue(), 
            (float)opacitySlider.getValue()
        );
    }

    private void displayPreviewDialog(Image img) {
        Stage s = new Stage(); 
        ImageView iv = new ImageView(img); 
        iv.setFitHeight(600); 
        iv.setPreserveRatio(true);
        s.setScene(new Scene(new VBox(iv), 500, 650)); 
        s.show();
    }

    @Override
    protected boolean isInputValid() { 
        return super.isInputValid() && !watermarkInput.getText().trim().isEmpty(); 
    }

    private record WatermarkConfig(String text, float fontSize, java.awt.Color color, float rotation, float opacity) {}
}