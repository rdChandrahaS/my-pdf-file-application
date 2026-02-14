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

    // FIX: Define constants to avoid "Hardcoded String" issues for static paths
    private static final String MAC_FONT_USER = "/Library/Fonts";
    private static final String MAC_FONT_SYSTEM = "/System/Library/Fonts";
    private static final String LINUX_FONT_SHARE = "/usr/share/fonts";
    private static final String LINUX_FONT_LOCAL = "/usr/local/share/fonts";
    private static final String LOCAL_APP_FONTS = "fonts";

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
            // FIX: Use "SystemRoot" environment variable (e.g., C:\Windows) instead of hardcoding "C:"
            String winDir = System.getenv("SystemRoot");
            if (winDir == null) {
                winDir = "C:\\Windows"; // Fallback only if env var is missing
            }
            dirs.add(new File(winDir, "Fonts"));
        } else if (os.contains("mac")) {
            // Use constants
            dirs.add(new File(MAC_FONT_USER));
            dirs.add(new File(MAC_FONT_SYSTEM));
        } else {
            // Use constants for Linux/Unix
            dirs.add(new File(LINUX_FONT_SHARE));
            dirs.add(new File(LINUX_FONT_LOCAL));
            dirs.add(new File(LOCAL_APP_FONTS)); 
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