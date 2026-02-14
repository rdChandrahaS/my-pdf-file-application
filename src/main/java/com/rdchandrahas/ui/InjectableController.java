package com.rdchandrahas.ui;

import com.rdchandrahas.core.NavigationService;

/**
 * InjectableController defines a contract for UI controllers that require 
 * access to the application's navigation logic.
 * * This interface allows the navigation engine to automatically inject the 
 * NavigationService instance after a view is loaded, enabling decoupled 
 * screen switching and tool routing.
 */
public interface InjectableController {

    /**
     * Injects the NavigationService into the controller instance.
     * This is typically called by the NavigationService itself during 
     * the FXML loading process.
     * * @param navService The central navigation handler for the application.
     */
    void setNavigationService(NavigationService navService);
}