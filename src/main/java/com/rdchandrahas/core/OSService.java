package com.rdchandrahas.core;

import java.io.File;
import java.util.List;

/**
 * OSService provides an interface for interacting with operating system-level features.
 * This abstraction allows the application to handle platform-specific tasks like
 * font location discovery and browser integration across different environments.
 */
public interface OSService {
    
    /**
     * Retrieves a list of standard directories where the operating system stores fonts.
     * This is used to scan for available .ttf files on the system.
     * * @return A list of File objects representing system font directories.
     */
    List<File> getSystemFontDirectories();
    
    /**
     * Opens the provided URL in the default system web browser.
     * * @param url The web address to be opened.
     */
    void openBrowser(String url);
    
    /**
     * Returns the name of the operating system currently running the application.
     * * @return A string containing the OS name (e.g., "Windows 11", "Linux").
     */
    String getOSName();
}