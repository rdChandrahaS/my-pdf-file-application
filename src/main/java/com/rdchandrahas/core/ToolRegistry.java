package com.rdchandrahas.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * ToolRegistry serves as the central discovery point for all PDF utility modules.
 * It utilizes the Java Service Provider Interface (SPI) to dynamically load Tool 
 * implementations at runtime without hardcoding them.
 */
public class ToolRegistry {

    private ToolRegistry() {
        throw new IllegalStateException("Utility class");
    }
    
    /** The internal list of discovered and loaded tools. */
    private static final List<Tool> tools = new ArrayList<>();

    static {
        /*
         * ServiceLoader scans the META-INF/services/com.rdchandrahas.core.Tool file
         * to find all registered implementations of the Tool interface.
         */
        ServiceLoader<Tool> loader = ServiceLoader.load(Tool.class);
        for (Tool tool : loader) {
            tools.add(tool);
        }
    }

    /**
     * Retrieves the list of all tools currently registered in the system.
     * This is typically used by the Dashboard to generate the tool grid.
     * * @return A list of available Tool implementations.
     */
    public static List<Tool> getTools() { 
        return tools; 
    }
}