package com.rdchandrahas.core;

import javafx.application.HostServices;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DefaultOSService provides a concrete implementation of the OSService interface.
 * It handles platform-specific logic such as identifying system font paths and 
 * delegating web browsing tasks to the native operating system.
 */
public class DefaultOSService implements OSService {
    private final HostServices hostServices;

    /**
     * Constructs the service with JavaFX HostServices.
     * * @param hostServices The JavaFX services used to interact with the native desktop.
     */
    public DefaultOSService(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Determines the standard system font directories based on the current operating system.
     * This allows the application to scan for available .ttf files locally.
     * * @return A list of File objects pointing to known font locations.
     */
    @Override
    public List<File> getSystemFontDirectories() {
        List<File> dirs = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Standard Windows font location
            dirs.add(new File("C:\\Windows\\Fonts"));
        } else if (os.contains("mac")) {
            // Common macOS font locations
            dirs.add(new File("/Library/Fonts"));
            dirs.add(new File("/System/Library/Fonts"));
        } else {
            // Standard Linux/Unix font locations
            dirs.add(new File("/usr/share/fonts"));
            dirs.add(new File("/usr/local/share/fonts"));
            // Custom relative path for distributed packages
            dirs.add(new File("fonts")); 
        }
        return dirs;
    }

    /**
     * Opens the default system web browser to a specific URL.
     * * @param url The web address to navigate to.
     */
    @Override
    public void openBrowser(String url) {
        if (hostServices != null) {
            hostServices.showDocument(url);
        }
    }

    /**
     * Retrieves the raw name of the operating system from the JVM.
     * * @return A string representing the OS (e.g., "Windows 11", "Linux").
     */
    @Override
    public String getOSName() {
        return System.getProperty("os.name");
    }
}