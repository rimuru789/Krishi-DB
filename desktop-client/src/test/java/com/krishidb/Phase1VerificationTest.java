package com.krishidb;

import com.formdev.flatlaf.FlatLightLaf;
import com.krishidb.dao.ProductDAO;
import com.krishidb.dao.SettingsDAO;
import com.krishidb.dao.SyncQueueDAO;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.model.Product;
import com.krishidb.model.SyncRecord;
import com.krishidb.ui.MainFrame;
import com.krishidb.util.I18n;

import javax.swing.*;
import java.util.List;

public class Phase1VerificationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("=== STARTING PHASE 1 VERIFICATION ===");

        // 1. Initialize Database
        System.out.println("\n--- Step 1: Initializing Database ---");
        DatabaseInitializer.initialize();
        System.out.println("Database initialization completed successfully.");

        // 2. Test SettingsDAO and language persistence
        System.out.println("\n--- Step 2: Testing SettingsDAO ---");
        SettingsDAO settingsDAO = new SettingsDAO();
        String originalLang = settingsDAO.getSetting("app_language", "en");
        System.out.println("Current saved language: " + originalLang);

        // Test saving language
        boolean savedMr = settingsDAO.saveSetting("app_language", "mr");
        if (!savedMr) throw new AssertionError("Failed to save 'mr' language to app_settings");
        String readMr = settingsDAO.getSetting("app_language", "en");
        System.out.println("Saved & verified 'mr': " + readMr);
        if (!"mr".equals(readMr)) throw new AssertionError("Expected 'mr', got: " + readMr);

        boolean savedHi = settingsDAO.saveSetting("app_language", "hi");
        String readHi = settingsDAO.getSetting("app_language", "en");
        System.out.println("Saved & verified 'hi': " + readHi);
        if (!"hi".equals(readHi)) throw new AssertionError("Expected 'hi', got: " + readHi);

        // Restore English as default
        settingsDAO.saveSetting("app_language", "en");
        System.out.println("Restored default language to 'en'");

        // 3. Test Centralized Multilingual System (I18n)
        System.out.println("\n--- Step 3: Testing I18n Translations ---");

        // English
        I18n.setLanguage("en");
        System.out.println("[EN] app.title: " + I18n.get("app.title"));
        System.out.println("[EN] nav.dashboard: " + I18n.get("nav.dashboard"));
        System.out.println("[EN] nav.inventory: " + I18n.get("nav.inventory"));
        System.out.println("[EN] nav.sync: " + I18n.get("nav.sync"));
        System.out.println("[EN] status.pending_changes: " + I18n.get("status.pending_changes", 5));
        if (!"Dashboard".equals(I18n.get("nav.dashboard"))) throw new AssertionError("EN translation mismatch");

        // Marathi
        I18n.setLanguage("mr");
        System.out.println("[MR] app.title: " + I18n.get("app.title"));
        System.out.println("[MR] nav.dashboard: " + I18n.get("nav.dashboard"));
        System.out.println("[MR] nav.inventory: " + I18n.get("nav.inventory"));
        System.out.println("[MR] nav.sync: " + I18n.get("nav.sync"));
        System.out.println("[MR] status.pending_changes: " + I18n.get("status.pending_changes", 5));
        if (!"डॅशबोर्ड".equals(I18n.get("nav.dashboard"))) throw new AssertionError("MR translation mismatch");

        // Hindi
        I18n.setLanguage("hi");
        System.out.println("[HI] app.title: " + I18n.get("app.title"));
        System.out.println("[HI] nav.dashboard: " + I18n.get("nav.dashboard"));
        System.out.println("[HI] nav.inventory: " + I18n.get("nav.inventory"));
        System.out.println("[HI] nav.sync: " + I18n.get("nav.sync"));
        System.out.println("[HI] status.pending_changes: " + I18n.get("status.pending_changes", 5));
        if (!"डैशबोर्ड".equals(I18n.get("nav.dashboard"))) throw new AssertionError("HI translation mismatch");

        // Fallback test
        I18n.setLanguage("en");
        String fallbackValue = I18n.get("non.existent.key.xyz");
        if (!"non.existent.key.xyz".equals(fallbackValue)) throw new AssertionError("Expected key as fallback");

        // 4. Test Existing Inventory (ProductDAO)
        System.out.println("\n--- Step 4: Testing Existing ProductDAO Functionality ---");
        ProductDAO productDAO = new ProductDAO();
        int initialPending = productDAO.getPendingCount();
        int initialTotal = productDAO.getAllProducts().size();
        System.out.println("Initial total products: " + initialTotal + ", pending: " + initialPending);

        // Test creating product
        Product testProduct = new Product("Phase1 Test Seed", "Seeds", "kg", 120.0, 25.0, 5.0);
        boolean added = productDAO.addProduct(testProduct);
        if (!added) throw new AssertionError("Failed to add test product via ProductDAO");
        System.out.println("Successfully added test product with generated ID via ProductDAO");

        // Verify it was added and queued
        List<Product> productsAfter = productDAO.getAllProducts();
        Product createdProduct = null;
        for (Product p : productsAfter) {
            if ("Phase1 Test Seed".equals(p.getName())) {
                createdProduct = p;
                break;
            }
        }
        if (createdProduct == null) throw new AssertionError("Test product not found in database");
        System.out.println("Verified product created in SQLite with ID: " + createdProduct.getId());

        // Verify sync queue has the INSERT operation
        SyncQueueDAO queueDAO = new SyncQueueDAO();
        List<SyncRecord> history = queueDAO.getSyncHistory();
        if (history.isEmpty() || !"products".equals(history.get(0).getTableName())) {
            throw new AssertionError("Sync queue does not contain latest product INSERT event");
        }
        System.out.println("Verified sync queue record created: " + history.get(0).getTableName() + " | " + history.get(0).getOperation());

        // Clean up test product
        boolean deleted = productDAO.deleteProduct(createdProduct.getId());
        if (!deleted) throw new AssertionError("Failed to delete test product");
        System.out.println("Cleaned up test product (ID " + createdProduct.getId() + ").");

        // 5. Test MainFrame UI and CardLayout Routing
        System.out.println("\n--- Step 5: Testing MainFrame CardLayout Routing ---");
        FlatLightLaf.setup();
        final MainFrame[] frameHolder = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frameHolder[0] = frame;

            String[] routes = {
                    "DASHBOARD", "NEW_SALE", "NEW_ENTRY", "INVENTORY",
                    "CUSTOMERS", "SUPPLIERS", "PURCHASES", "EXPENSES",
                    "TRANSACTIONS", "REPORTS", "MARKET_PRICES", "SYNC", "SETTINGS"
            };

            for (String route : routes) {
                frame.showPage(route);
            }
            System.out.println("Successfully navigated all 13 CardLayout routes without errors.");

            // Test dynamic language change while UI is active
            I18n.setLanguage("mr");
            System.out.println("Switched to Marathi in active UI.");
            I18n.setLanguage("hi");
            System.out.println("Switched to Hindi in active UI.");
            I18n.setLanguage("en");
            System.out.println("Switched back to English in active UI.");

            frame.dispose();
        });

        System.out.println("\n=== ALL PHASE 1 VERIFICATION CHECKS PASSED SUCCESSFULLY ===");
    }
}
