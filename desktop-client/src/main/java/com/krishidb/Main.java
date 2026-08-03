package com.krishidb;

import com.formdev.flatlaf.FlatLightLaf;
import com.krishidb.dao.ProductDAO;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.ui.MainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        // Initialize the local offline database
        DatabaseInitializer.initialize();

        // Enable FlatLaf
        FlatLightLaf.setup();

        // Launch the graphical interface
        SwingUtilities.invokeLater(() -> {

            MainFrame mainFrame = new MainFrame();

            mainFrame.setVisible(true);
        });

        ProductDAO dao = new ProductDAO();

System.out.println(
    "Pending products: "
    + dao.getPendingCount()
);
    }


    
    
}