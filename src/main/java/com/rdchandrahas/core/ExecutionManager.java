package com.rdchandrahas.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ExecutionManager handles the application's threading model.
 * It provides a centralized way to submit tasks for either background (asynchronous) 
 * or foreground (synchronous) execution, and manages the lifecycle of the executor services.
 */
public class ExecutionManager {
    
    private static final Logger LOGGER = Logger.getLogger(ExecutionManager.class.getName());
    
    // Safety limit to guarantee we always have at least 2 threads, but max out at CPU cores.
    // This prevents the "Thread Bomb" when uploading 5,000 files.
    private static final int MAX_THREADS = Math.max(2, Runtime.getRuntime().availableProcessors());
    
    private ExecutionManager() {
        throw new IllegalStateException("Utility class");
    }
    
    // --- State Variables ---
    
    /** Default: Run in background (Async) to keep the UI responsive. */
    private static boolean async = true;
    
    /** Flag to track if multi-threading is active or if tasks are serialized on a single thread. */
    private static boolean multiThreadingEnabled = true;

    /** The internal executor service used to manage thread pools. */
    private static ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

    // --- Configuration Methods ---

    /**
     * Toggles between background threading and blocking execution.
     * @param enabled true for background threads (Async), false for blocking the UI thread (Sync).
     */
    public static void setAsync(boolean enabled) {
        async = enabled;
        LOGGER.log(Level.INFO, "Execution Mode: {0}", (async ? "Background Threads" : "Blocking/Sync"));
    }

    /**
     * Toggles between multi-threaded execution and single-thread execution.
     * If the mode changes, the current executor is shut down and replaced with the appropriate pool type.
     * @param enabled true for multi-threading (Pool), false for single background thread (Serial).
     */
    public static void setMultiThreading(boolean enabled) {
        // Prevent redundant restarts if the state hasn't changed
        if (multiThreadingEnabled == enabled) return;
        
        multiThreadingEnabled = enabled;
        ExecutorService oldExecutor = executor;
        
        if (multiThreadingEnabled) {
            // Create a pool optimized for the system's CPU core count
            executor = Executors.newFixedThreadPool(MAX_THREADS);
        } else {
            // Create an executor that handles tasks one by one in order
            executor = Executors.newSingleThreadExecutor();
        }
        
        // Gracefully shut down the previous executor service
        if (oldExecutor != null && !oldExecutor.isShutdown()) {
            oldExecutor.shutdown();
        }
        
        LOGGER.log(Level.INFO, "Multi-threading Mode: {0}", (multiThreadingEnabled ? "Enabled" : "Disabled"));
    }

    /**
     * Checks if multi-threading is currently active.
     * @return true if multi-threading is enabled.
     */
    public static boolean isMultiThreadingEnabled() {
        return multiThreadingEnabled;
    }

    // --- Execution Methods ---

    /**
     * Submits a task for execution based on the current 'async' configuration.
     * @param task The Runnable task to be performed.
     */
    public static void submit(Runnable task) {
        if (async) {
            // Run in a background thread to ensure the UI remains responsive
            executor.submit(task);
        } else {
            // Run immediately on the current (UI) thread, which will cause the interface to freeze until finished
            try {
                task.run();
            } catch (Exception e) {
                // Log errors occurring during synchronous execution instead of printStackTrace
                LOGGER.log(Level.SEVERE, "Task execution failed during synchronous mode", e);
            }
        }
    }

    /**
     * Initiates an orderly shutdown of the executor service.
     * Should be called when the application is closing to release system resources.
     */
    public static void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}