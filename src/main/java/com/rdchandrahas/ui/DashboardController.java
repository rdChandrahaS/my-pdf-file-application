package com.rdchandrahas.ui;

import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.core.Tool;
import com.rdchandrahas.core.ToolRegistry;
import com.rdchandrahas.ui.base.BaseToolController;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DashboardController manages the main entry point of the application.
 */
public class DashboardController implements InjectableController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private FlowPane toolFlowPane;
    private NavigationService navigationService;

    @Override
    public void setNavigationService(NavigationService navService) {
        this.navigationService = navService;
    }

    @FXML
    public void initialize() {
        renderToolCards();
        
        toolFlowPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            String sizeClass = (newVal.doubleValue() < 600) ? "compact-tool" : "standard-tool";
            toolFlowPane.getChildren().forEach(node -> {
                node.getStyleClass().removeAll("standard-tool", "compact-tool");
                node.getStyleClass().add(sizeClass);
            });
        });
    }

    private void renderToolCards() {
        toolFlowPane.getChildren().clear();
        for (Tool tool : ToolRegistry.getTools()) {
            VBox card = createToolCard(tool);
            toolFlowPane.getChildren().add(card);
        }
    }

    private VBox createToolCard(Tool tool) {
        VBox card = new VBox(15); 
        card.getStyleClass().add("tool-card"); 
        card.setAlignment(Pos.CENTER);
        
        Node iconNode = null;

        // 1. Primary: Try loading Image from Path
        if (tool.getIconPath() != null && !tool.getIconPath().trim().isEmpty()) {
            iconNode = extractedIconNode(tool);
        }

        // 2. Secondary: Fallback to FontIcon (Ikonli)
        if (iconNode == null && tool.getIconCode() != null && !tool.getIconCode().trim().isEmpty()) {
            iconNode = getFontIcon(tool);
        }

        // 3. Absolute Fallback: Placeholder label
        if (iconNode == null) {
            iconNode = getPlaceholder();
        }
        
        Label title = new Label(tool.getName());
        title.getStyleClass().add("tool-title");

        card.getChildren().addAll(iconNode, title);

        // --- Interaction Logic ---
        card.setOnMouseClicked(e -> {
            try {
                Class<?> controllerClass = tool.getControllerClass();
                if (controllerClass != null) {
                    BaseToolController controller = (BaseToolController) controllerClass.getDeclaredConstructor().newInstance();
                    navigationService.navigateToTool(controller);
                } else {
                    // FIXED SONARQUBE SMELL: Replaced System.err
                    LOGGER.log(Level.WARNING, "Controller class not defined for: {0}", tool.getName());
                }
            } catch (Exception ex) {
                // FIXED SONARQUBE SMELL: Replaced System.err and printStackTrace
                LOGGER.log(Level.SEVERE, "Failed to open tool: " + tool.getName(), ex);
            }
        });
        
        String description = tool.getDescription();
        if (description != null && !description.isBlank()) {
            Tooltip tooltip = new Tooltip(description);
            tooltip.setStyle("-fx-font-size: 12px;");
            Tooltip.install(card, tooltip);
        }
        
        return card;
    }


    private Node getPlaceholder() {
        Label placeholder = new Label("?");
        placeholder.setStyle("-fx-font-size: 40px; -fx-text-fill: #0078d7;");
        return placeholder;
    }

    private Node getFontIcon(Tool tool) {
        FontIcon fontIcon = new FontIcon(tool.getIconCode());
        fontIcon.getStyleClass().add("tool-icon");
        return fontIcon;
    }

    private Node extractedIconNode(Tool tool) {
        try {
            InputStream imageStream = getClass().getResourceAsStream(tool.getIconPath());
            if (imageStream != null) {
                return getClip(imageStream);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load icon image: {0}", tool.getIconPath());
        }
        return null;
    }

    private Node getClip(InputStream imageStream) {
        ImageView imageView = new ImageView(new Image(imageStream));
        
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(100, 100);
        clip.setArcWidth(45); 
        clip.setArcHeight(45);
        imageView.setClip(clip);

        imageView.getStyleClass().add("tool-image-icon"); 
        
        return imageView;
    }
}