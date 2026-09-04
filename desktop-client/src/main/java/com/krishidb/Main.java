package com.krishidb;

import com.formdev.flatlaf.FlatLightLaf;
import com.krishidb.dao.ProductDAO;
import com.krishidb.dao.SyncQueueDAO;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.model.SyncRecord;
import com.krishidb.ui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        
        DatabaseInitializer.initialize();

        // Load saved language preference before creating UI
        com.krishidb.dao.SettingsDAO settingsDAO = new com.krishidb.dao.SettingsDAO();
        String savedLanguage = settingsDAO.getSetting("app_language", "en");
        com.krishidb.util.I18n.setLanguage(savedLanguage);

        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });

        ProductDAO dao = new ProductDAO();
        System.out.println("Pending products: " + dao.getPendingCount());
    }


    
    
}