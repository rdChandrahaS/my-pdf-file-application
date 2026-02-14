package com.rdchandrahas.ui;

import com.rdchandrahas.core.NavigationService;
import com.rdchandrahas.shared.component.FileListView;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;

/**
 * GenericToolController acts as a standard implementation of SortableToolController.
 * It is designed to be used in FXML files that require standard file listing and 
 * sorting capabilities without needing custom business logic in the controller itself.
 */

public class GenericToolController implements SortableToolController {
    
    /*
     * Implementation Note:
     * To use this, set the fx:controller in your FXML to:
     * com.rdchandrahas.ui.GenericToolController
     */

    @FXML private FileListView fileListView;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ToggleButton listViewBtn;
    @FXML private ToggleButton gridViewBtn;

    private NavigationService navigationService;

    /**
     * Initializes the controller by setting up the default sort options
     * and view switching logic provided by the SortableToolController interface.
     */
    @FXML
    public void initialize() {
        setupSortAndViews();
    }

    // --- SortableToolController Interface Implementation ---

    @Override 
    public FileListView getFileListView() { 
        return fileListView; 
    }

    @Override 
    public ComboBox<String> getSortCombo() { 
        return sortCombo; 
    }

    @Override 
    public ToggleButton getListViewBtn() { 
        return listViewBtn; 
    }

    @Override 
    public ToggleButton getGridViewBtn() { 
        return gridViewBtn; 
    }

    @Override
    public void setNavigationService(NavigationService navService) {
        this.navigationService = navService;
    }

    /**
     * Navigates the user back to the main dashboard.
     */
    @FXML
    private void handleBack() {
        if (navigationService != null) {
            navigationService.navigateTo("/ui/Dashboard.fxml");
        }
    }

    // --- FXML Event Handlers ---

    @FXML 
    private void onSortAction() { 
        handleSort(); 
    }

    @FXML 
    private void onListToggle() { 
        switchToList(); 
    }

    @FXML 
    private void onGridToggle() { 
        switchToGrid(); 
    }
}