package com.rdchandrahas.core;

/**
 * The Tool interface defines the contract for individual PDF utility modules.
 * Implementing this interface allows a feature (like Merge, Split, or Protect) 
 * to be dynamically discovered and integrated into the application's dashboard.
 */
public interface Tool {
    
    /**
     * Returns the display name of the tool (e.g., "Merge PDF").
     * * @return The user-friendly name of the tool.
     */
    String getName();

    /**
     * Returns the resource path to the FXML layout specific to this tool's configuration.
     * * @return The FXML resource path.
     */
    String getFxmlPath();

    /**
     * Returns the Class type of the controller associated with this tool.
     * Used for dynamic instantiation and linking during navigation.
     * * @return The controller class.
     */
    Class<?> getControllerClass();
    
    /**
     * Returns a font icon code (e.g., Ikonli/FontAwesome) to represent the tool visually.
     * * @return The icon code string, or null if not used.
     */
    default String getIconCode() { 
        return null; 
    }
    
    /**
     * Returns a resource path to an image-based icon for the tool.
     * * @return The icon image path, or null if not used.
     */
    default String getIconPath() { 
        return null; 
    }
    
    /**
     * Provides a brief summary of what the tool does for UI tooltips or descriptions.
     * * @return A short description string.
     */
    default String getDescription() { 
        return ""; 
    }
}