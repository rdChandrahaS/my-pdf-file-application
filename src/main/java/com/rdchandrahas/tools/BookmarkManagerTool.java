package com.rdchandrahas.tools;

import com.rdchandrahas.core.Tool;

public class BookmarkManagerTool implements Tool {

    @Override
    public String getName() {
        return "Bookmark Manager";
    }

    @Override
    public String getDescription() {
        return "Add bookmarks (Table of Contents) to a PDF document.";
    }

    @Override
    public String getFxmlPath() {
        return "/ui/ToolLayout.fxml";
    }

    @Override
    public String getIconCode() {
        return "fas-bookmark"; 
    }

    @Override
    public String getIconPath() {
        return "/icons/tools/bookmark_pdf.png";
    }

    @Override
    public Class<?> getControllerClass() {
        return com.rdchandrahas.ui.BookmarkManagerController.class;
    }
}