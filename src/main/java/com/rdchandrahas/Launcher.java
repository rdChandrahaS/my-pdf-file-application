package com.rdchandrahas;

/**
 * The Launcher class serves as the main entry point for the application.
 * It acts as a wrapper around the JavaFX MainApp class.
 *
 * Using a separate Launcher class that does not extend javafx.application.Application 
 * is a standard workaround to prevent the "JavaFX runtime components are missing" 
 * error when running the project as a non-modular (classpath-based) application 
 * or when building a fat/executable JAR file.
 */
public class Launcher {
    
    /**
     * The main method that triggers the application launch.
     * * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        // Delegates the execution to the actual JavaFX application class
        MainApp.main(args);
    }
}