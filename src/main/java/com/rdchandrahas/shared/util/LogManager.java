package com.rdchandrahas.shared.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {
    private static final String LOG_FILE = "stress_test.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LogManager() {
        throw new IllegalStateException("Utility class"); // Fixed SonarCloud smell!
    }

    public static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // Print to console for real-time viewing
        System.out.println(logEntry);

        // Write to file (Append mode)
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Could not write to log file: " + e.getMessage());
        }
    }
}