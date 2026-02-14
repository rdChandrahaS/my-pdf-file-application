# PDFGear: Advanced JavaFX PDF Toolkit

A modular, high-performance, open-source desktop application for manipulating and processing PDF documents. Built with a clean architecture, asynchronous processing, and an intuitive, highly customizable user interface.

---

## ✨ Features

PDFGear offers a comprehensive suite of tools designed to handle everyday PDF tasks securely and locally on your machine:

**Document Manipulation**
* **Merge PDF:** Combine multiple PDF files into one single document in your preferred order.
* **Split PDF:** Divide a single PDF into multiple files based on page ranges or specific criteria.
* **Delete Pages:** Remove specific pages or page ranges from your document.
* **Rearrange Pages:** Change the order of pages by specifying a new sequence.
* **Rotate PDF:** Correct page orientations by rotating 90, 180, or 270 degrees.
* **Add Page Numbers:** Insert dynamic, customizable page numbering.

**Conversion & Extraction**
* **Compress PDF:** Iteratively reduce file size by a target percentage or specific MB/KB target using intelligent image downscaling.
* **Image to PDF:** Convert multiple PNG and JPEG images into a single, high-quality PDF.
* **PDF to Image:** Render and extract PDF pages into high-resolution PNG or JPEG files.
* **Extract Text:** Instantly pull all readable text from a PDF into a clean `.txt` file.
* **Extract Images:** Scan and save all embedded raster images from your documents.

**Security & Branding**
* **Protect PDF:** Secure files with user/owner passwords and restrict printing/copying permissions.
* **Unlock PDF:** Remove security restrictions and passwords from authorized documents.
* **Watermark PDF:** Apply custom text stamps to all pages to indicate ownership or status.

---

## 🛠️ Built With

* **[Java 17+](https://adoptium.net/):** The core programming language.
* **[JavaFX](https://openjfx.io/):** For rendering the modern, responsive desktop UI.
* **[Apache PDFBox](https://pdfbox.apache.org/):** The heavy-lifting engine for parsing, rendering, and manipulating PDF streams.
* **[Ikonli (FontAwesome)](https://kordamp.org/ikonli/):** For scalable, high-quality vector icons across the UI.
* **Custom Async Engine:** Utilizes a bespoke `ExecutionManager` to offload heavy PDF rendering to background threads, keeping the UI stutter-free.

---

## 📖 Menu Guide

The top navigation menu provides quick access to global app functions, deep customizations, and developer tools:

### **File**
* **Exit:** Safely shuts down the application and ensures all temporary files are cleaned up.

### **Appearance**
* **Theme:** Instantly switch the application interface between **Light Mode** and **Dark Mode**.
* **Accent Color:** Personalize the UI by choosing a custom highlight color (Blue, Green, Red, Purple, Orange, Teal).
* **Font Size:** Scale the global interface text size (Small, Medium, Large, Extra Large) for better readability.

### **Performance**
* **Memory Limit:** Set the maximum RAM allocation (e.g., 512MB, 1GB) before the app falls back to disk-based processing for massive files.
* **Cache Size:** Define the maximum memory limit (in MB) for the UI's thumbnail image cache.
* **Background Processing:** Toggle asynchronous execution. When enabled, heavy PDF tasks won't freeze the user interface.
* **Hardware Acceleration:** Enable/disable GPU rendering for the UI (requires an app restart to take effect).

### **Shortcuts**
* **View All:** Opens a cheat sheet of active system keyboard shortcuts (e.g., Undo, Save, Clear).
* **Customize:** (Coming Soon) Currently in read-only mode; allows future remapping of default keybinds.
* **Reset:** Restores all keyboard shortcuts to their factory default settings.

### **Developer Mode**
* **Debug Console:** Opens a live, dedicated window displaying internal system logs and processes.
* **Verbose Logging:** Toggles extra-detailed terminal output for debugging file processing steps.
* **API Testing:** Runs diagnostic health checks on the PDFBox Engine, Navigation, and OS Integration services.
* **Feature Flags:** A control panel to toggle in-development features like Cloud Sync or Multi-threaded execution.
* **Plugin Manager:** Scans the local `/plugins` directory for external `.jar` modules and displays their load status.

### **Advanced**
* **Experimental Features:** Unlock unstable, cutting-edge tools (like AI Summarizer or Batch OCR) — *Note: Disabled by default in production builds.*
* **Reset App State:** A panic button that permanently reverts all app settings, themes, and memory limits back to a fresh install state.
* **Clear Cache:** Manually purges all stored thumbnail memory and erases leftover PDF temp files from your hard drive.

### **Help**
* **Check for Updates:** Pings the GitHub API to check if a newer version of PDFGear is available to download.
* **GitHub Repository:** Opens your default web browser to the project's source code page.
* **About:** Displays the current version, developer credits, and app info.

---

## 🚀 How to Add Your Own Tool Easily

PDFGear is built with a dynamic `ToolRegistry` and a standardized `BaseToolController`. Adding a new feature takes just 3 simple steps:

### Step 1: Create the Tool Definition
Implement the `Tool` interface in the `com.rdchandrahas.tools` package. This tells the Dashboard how to display your tool.

```java
package com.rdchandrahas.tools;
import com.rdchandrahas.core.Tool;

public class MyCustomTool implements Tool {
    @Override public String getName() { return "My Custom Tool"; }
    @Override public String getDescription() { return "Does something awesome to PDFs."; }
    @Override public String getFxmlPath() { return "/ui/ToolLayout.fxml"; } // Use the standard layout
    @Override public String getIconCode() { return "fas-magic"; } // FontAwesome icon
    @Override public String getIconPath() { return null; } // Optional: path to an image asset like "/icons/magic.png"
    @Override public Class<?> getControllerClass() { return com.rdchandrahas.ui.MyCustomController.class; }
}
```

### Step 2: Create the Logic (Controller)
Extend `BaseToolController`. You immediately get access to async processing, safe file loading, and UI error handling for free.

```java
package com.rdchandrahas.ui;
import com.rdchandrahas.ui.base.BaseToolController;

public class MyCustomController extends BaseToolController {
    
    @Override
    protected void onInitialize() {
        setTitle("My Custom Tool");
        setActionText("Run Magic");
        // Optionally add custom UI elements to the toolbar here:
        // addToolbarItem(new TextField("Custom Input"));
    }

    @Override
    protected void handleAddFiles() {
        addFiles("PDF Files", "*.pdf");
    }

    @Override
    protected void handleAction() {
        // processWithSaveDialog handles the UI loader and background thread automatically!
        processWithSaveDialog("Save Output", "magic_output.pdf", (destination) -> {
            
            // 1. Get your input files
            String inputPath = fileListView.getItems().get(0).getPath();
            
            // 2. Do your PDFBox logic here (loadDocumentSafe is provided by BaseToolController)
            try (var doc = loadDocumentSafe(inputPath)) {
                // ... modify the document ...
                doc.save(destination);
            }
        });
    }
}
```

### Step 3: Register the Tool
Open `src/main/resources/META-INF/services/com.rdchandrahas.core.Tool` and append your fully qualified class name to the end of the list:

`eg.:    com.rdchandrahas.tools.MergePdfTool`


## That's it! ServiceLoader will detect your tool on startup, and the Dashboard will automatically render a new card for it, handle the routing, and bind your background logic to the UI.
