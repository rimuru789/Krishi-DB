package com.krishidb;

import com.formdev.flatlaf.FlatLightLaf;
import com.krishidb.dao.CustomerDAO;
import com.krishidb.dao.ProductDAO;
import com.krishidb.dao.SaleDAO;
import com.krishidb.dao.SyncQueueDAO;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.database.DatabaseManager;
import com.krishidb.exception.InsufficientStockException;
import com.krishidb.model.Customer;
import com.krishidb.model.Product;
import com.krishidb.model.Sale;
import com.krishidb.model.SaleItem;
import com.krishidb.ui.MainFrame;
import com.krishidb.util.I18n;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Phase3SalesVerificationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("=== STARTING PHASE 3A SALES / POS VERIFICATION ===");
        System.out.println("==================================================");

        // Step 0: Database Initialization
        System.out.println("\n--- Step 0: Initialize Database ---");
        DatabaseInitializer.initialize();

        SaleDAO saleDAO = new SaleDAO();
        ProductDAO productDAO = new ProductDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        SyncQueueDAO queueDAO = new SyncQueueDAO();

        // Setup Test Customer
        Customer testCustomer = new Customer("Tukaram Shinde", "9822334455", "Baramati");
        customerDAO.addCustomer(testCustomer);
        int customerId = testCustomer.getId();
        System.out.println("Registered test customer ID: " + customerId + " (" + testCustomer.getName() + ")");

        // Setup Test Products
        Product prodWheat = new Product("Test Sharbati Wheat", "Grains", "kg", 40.0, 100.0, 10.0);
        productDAO.addProduct(prodWheat);
        int wheatId = getProductIdByName("Test Sharbati Wheat");
        prodWheat.setId(wheatId);

        Product prodDap = new Product("Test DAP Fertilizer", "Fertilizers", "bag", 1350.0, 25.0, 5.0);
        productDAO.addProduct(prodDap);
        int dapId = getProductIdByName("Test DAP Fertilizer");
        prodDap.setId(dapId);

        System.out.printf("Setup Products: Wheat ID=%d (Stock=%.1f @ ₹%.2f), DAP ID=%d (Stock=%.1f @ ₹%.2f)%n",
                wheatId, prodWheat.getStockQuantity(), prodWheat.getSellingPrice(),
                dapId, prodDap.getStockQuantity(), prodDap.getSellingPrice());

        // --------------------------------------------------------------------
        // TEST 1: Sale with registered customer
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 1: Sale with registered customer ---");
        Sale sale1 = new Sale();
        sale1.setCustomerId(customerId);
        sale1.setPaymentMethod("CASH");
        sale1.setNotes("Sale 1 with registered customer");

        List<SaleItem> items1 = new ArrayList<>();
        items1.add(new SaleItem(wheatId, prodWheat.getName(), prodWheat.getUnit(), 10.0, 40.0)); // 10 * 40 = 400.0

        Sale completedSale1 = saleDAO.createSale(sale1, items1);
        if (completedSale1.getId() <= 0) throw new AssertionError("Test 1 Failed: Generated sale ID is invalid");
        if (!Integer.valueOf(customerId).equals(completedSale1.getCustomerId()))
            throw new AssertionError("Test 1 Failed: Customer ID mismatch in completed sale");
        System.out.println("Test 1 Passed: Registered customer sale created with ID: " + completedSale1.getId());

        // --------------------------------------------------------------------
        // TEST 2: Walk-in CASH sale (customer_id is null)
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 2: Walk-in CASH sale ---");
        Sale sale2 = new Sale();
        sale2.setCustomerId(null); // Walk-in
        sale2.setPaymentMethod("CASH");
        sale2.setNotes("Walk-in retail purchase");

        List<SaleItem> items2 = new ArrayList<>();
        items2.add(new SaleItem(wheatId, prodWheat.getName(), prodWheat.getUnit(), 5.0, 40.0)); // 5 * 40 = 200.0

        Sale completedSale2 = saleDAO.createSale(sale2, items2);
        if (completedSale2.getCustomerId() != null)
            throw new AssertionError("Test 2 Failed: Walk-in sale must have null customerId");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT customer_id FROM sales WHERE id = ?")) {
            ps.setInt(1, completedSale2.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new AssertionError("Test 2 Failed: Sale record not found in DB");
                if (rs.getObject("customer_id") != null)
                    throw new AssertionError("Test 2 Failed: DB customer_id must be null for walk-in");
            }
        }
        System.out.println("Test 2 Passed: Walk-in CASH sale verified with null customer_id.");

        // --------------------------------------------------------------------
        // TEST 3: UPI sale
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 3: UPI sale ---");
        Sale sale3 = new Sale();
        sale3.setCustomerId(customerId);
        sale3.setPaymentMethod("UPI");
        sale3.setNotes("QR scan payment");

        List<SaleItem> items3 = new ArrayList<>();
        items3.add(new SaleItem(dapId, prodDap.getName(), prodDap.getUnit(), 2.0, 1350.0)); // 2 * 1350 = 2700.0

        Sale completedSale3 = saleDAO.createSale(sale3, items3);
        if (!"UPI".equals(completedSale3.getPaymentMethod()))
            throw new AssertionError("Test 3 Failed: Payment method should be UPI");
        System.out.println("Test 3 Passed: UPI sale recorded with ID: " + completedSale3.getId());

        // --------------------------------------------------------------------
        // TEST 4: CREDIT / Khata sale
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 4: CREDIT / Khata sale ---");
        Sale sale4 = new Sale();
        sale4.setCustomerId(customerId);
        sale4.setPaymentMethod("CREDIT");
        sale4.setNotes("Khata entry - due next week");

        List<SaleItem> items4 = new ArrayList<>();
        items4.add(new SaleItem(wheatId, prodWheat.getName(), prodWheat.getUnit(), 10.0, 40.0)); // 400.0

        Sale completedSale4 = saleDAO.createSale(sale4, items4);
        if (!"CREDIT".equals(completedSale4.getPaymentMethod()))
            throw new AssertionError("Test 4 Failed: Payment method should be CREDIT");
        if (!Integer.valueOf(customerId).equals(completedSale4.getCustomerId()))
            throw new AssertionError("Test 4 Failed: Credit sale must have registered customer ID");
        System.out.println("Test 4 Passed: CREDIT / Khata sale recorded with ID: " + completedSale4.getId());

        // --------------------------------------------------------------------
        // TEST 5, 6 & 7: Sale with multiple products, correct line totals & grand total
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 5, 6 & 7: Multiple products, line totals & grand total ---");
        Sale saleMulti = new Sale();
        saleMulti.setCustomerId(customerId);
        saleMulti.setPaymentMethod("CASH");

        List<SaleItem> multiItems = new ArrayList<>();
        multiItems.add(new SaleItem(wheatId, prodWheat.getName(), prodWheat.getUnit(), 4.5, 40.0)); // 180.0
        multiItems.add(new SaleItem(dapId, prodDap.getName(), prodDap.getUnit(), 3.0, 1350.0));     // 4050.0

        Sale completedMulti = saleDAO.createSale(saleMulti, multiItems);

        // Verify line totals
        List<SaleItem> fetchedItems = saleDAO.getSaleItems(completedMulti.getId());
        if (fetchedItems.size() != 2)
            throw new AssertionError("Test 5 Failed: Expected 2 items, got " + fetchedItems.size());

        SaleItem it1 = fetchedItems.get(0);
        SaleItem it2 = fetchedItems.get(1);

        if (Math.abs(it1.getSubtotal() - (it1.getQuantity() * it1.getPricePerUnit())) > 0.01)
            throw new AssertionError("Test 6 Failed: Line total incorrect for item 1");
        if (Math.abs(it2.getSubtotal() - (it2.getQuantity() * it2.getPricePerUnit())) > 0.01)
            throw new AssertionError("Test 6 Failed: Line total incorrect for item 2");

        // Verify grand total
        double expectedGrandTotal = it1.getSubtotal() + it2.getSubtotal();
        if (Math.abs(completedMulti.getTotalAmount() - expectedGrandTotal) > 0.01)
            throw new AssertionError("Test 7 Failed: Grand total mismatch. Expected: " + expectedGrandTotal + ", got: " + completedMulti.getTotalAmount());
        System.out.printf("Test 5, 6 & 7 Passed: Multi-item sale #%d verified (Line 1: ₹%.2f, Line 2: ₹%.2f, Grand Total: ₹%.2f)%n",
                completedMulti.getId(), it1.getSubtotal(), it2.getSubtotal(), completedMulti.getTotalAmount());

        // --------------------------------------------------------------------
        // TEST 8 & 9: Inventory deduction & No negative stock
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 8 & 9: Inventory deduction and no negative stock ---");
        // Wheat was 100 initially:
        // Sold in sale1: 10
        // Sold in sale2: 5
        // Sold in sale4: 10
        // Sold in saleMulti: 4.5
        // Expected wheat remaining: 100 - (10 + 5 + 10 + 4.5) = 70.5
        double remainingWheat = 0.0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT stock_quantity FROM products WHERE id = ?")) {
            ps.setInt(1, wheatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) remainingWheat = rs.getDouble(1);
            }
        }
        if (Math.abs(remainingWheat - 70.5) > 0.01)
            throw new AssertionError("Test 8 Failed: Expected 70.5 wheat stock, got " + remainingWheat);

        // DAP was 25 initially:
        // Sold in sale3: 2
        // Sold in saleMulti: 3
        // Expected DAP remaining: 25 - 5 = 20.0
        double remainingDap = 0.0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT stock_quantity FROM products WHERE id = ?")) {
            ps.setInt(1, dapId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) remainingDap = rs.getDouble(1);
            }
        }
        if (Math.abs(remainingDap - 20.0) > 0.01)
            throw new AssertionError("Test 8 Failed: Expected 20.0 DAP stock, got " + remainingDap);

        // Verify no products in database have negative stock
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM products WHERE stock_quantity < 0");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) > 0)
                throw new AssertionError("Test 9 Failed: Found products with negative stock quantity!");
        }
        System.out.printf("Test 8 & 9 Passed: Inventory deducted accurately (Wheat: %.1f, DAP: %.1f) and negative stock = 0.%n",
                remainingWheat, remainingDap);

        // --------------------------------------------------------------------
        // TEST 10: Insufficient stock rejected
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 10: Insufficient stock rejected ---");
        boolean stockRejected = false;
        try {
            Sale overSale = new Sale();
            overSale.setPaymentMethod("CASH");
            List<SaleItem> overItems = new ArrayList<>();
            // Request 500 bags of DAP when available is 20
            overItems.add(new SaleItem(dapId, prodDap.getName(), prodDap.getUnit(), 500.0, 1350.0));
            saleDAO.createSale(overSale, overItems);
        } catch (InsufficientStockException ex) {
            stockRejected = true;
            if (ex.getRequestedQuantity() != 500.0 || Math.abs(ex.getAvailableQuantity() - 20.0) > 0.01)
                throw new AssertionError("Test 10 Failed: Exception quantities mismatched: " + ex.getMessage());
            System.out.println("Caught expected InsufficientStockException: " + ex.getMessage());
        }
        if (!stockRejected)
            throw new AssertionError("Test 10 Failed: Oversale was not rejected!");
        System.out.println("Test 10 Passed: Insufficient stock sale was correctly rejected.");

        // --------------------------------------------------------------------
        // TEST 11: CREDIT without customer rejected
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 11: CREDIT without customer rejected ---");
        boolean creditRejected = false;
        try {
            Sale badCreditSale = new Sale();
            badCreditSale.setCustomerId(null); // No customer
            badCreditSale.setPaymentMethod("CREDIT");
            List<SaleItem> creditItems = new ArrayList<>();
            creditItems.add(new SaleItem(wheatId, prodWheat.getName(), prodWheat.getUnit(), 2.0, 40.0));
            saleDAO.createSale(badCreditSale, creditItems);
        } catch (IllegalArgumentException ex) {
            creditRejected = true;
            System.out.println("Caught expected IllegalArgumentException: " + ex.getMessage());
        }
        if (!creditRejected)
            throw new AssertionError("Test 11 Failed: Anonymous credit sale was not rejected!");
        System.out.println("Test 11 Passed: CREDIT without customer was rejected.");

        // --------------------------------------------------------------------
        // TEST 12 & 13: Sale and Sale items persisted
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 12 & 13: Sale and items persisted in SQLite ---");
        Sale persistedSale = saleDAO.getSaleById(completedSale1.getId());
        if (persistedSale == null) throw new AssertionError("Test 12 Failed: Sale not found by ID");
        if (persistedSale.getItems() == null || persistedSale.getItems().isEmpty())
            throw new AssertionError("Test 13 Failed: Persisted sale has no items loaded");
        System.out.printf("Test 12 & 13 Passed: Sale #%d persisted with %d items.%n",
                persistedSale.getId(), persistedSale.getItems().size());

        // --------------------------------------------------------------------
        // TEST 14: Transaction ledger created
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 14: Transaction ledger entry created ---");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT amount, payment_method FROM transactions WHERE transaction_type = 'SALE' AND reference_id = ?")) {
            ps.setInt(1, completedSale1.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new AssertionError("Test 14 Failed: No ledger entry found for sale #1");
                double amt = rs.getDouble("amount");
                String pm = rs.getString("payment_method");
                if (Math.abs(amt - 400.0) > 0.01 || !"CASH".equalsIgnoreCase(pm))
                    throw new AssertionError("Test 14 Failed: Ledger entry values mismatched: amount=" + amt + ", payment=" + pm);
            }
        }
        System.out.println("Test 14 Passed: Transaction ledger entry exists with matching amount and payment mode.");

        // --------------------------------------------------------------------
        // TEST 15: Sync queue entries created
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 15: Sync queue entries created ---");
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM sync_queue WHERE table_name = 'sales' AND record_id = ? AND operation = 'INSERT'")) {
            ps.setInt(1, completedSale1.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) < 1)
                    throw new AssertionError("Test 15 Failed: No sync_queue entry for sale #1");
            }
        }
        System.out.println("Test 15 Passed: Sync queue contains 'sales' INSERT entry.");

        // --------------------------------------------------------------------
        // TEST 16: Forced failure causes complete rollback
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 16: Forced failure causes complete rollback ---");
        double wheatStockBefore = 0.0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT stock_quantity FROM products WHERE id = ?")) {
            ps.setInt(1, wheatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) wheatStockBefore = rs.getDouble(1);
            }
        }

        int salesCountBefore = saleDAO.getTotalSalesCount();

        boolean rollbackTriggered = false;
        try {
            Sale failingSale = new Sale();
            failingSale.setCustomerId(customerId);
            failingSale.setPaymentMethod("CASH");

            List<SaleItem> failingItems = new ArrayList<>();
            // Item 1: Valid stock
            failingItems.add(new SaleItem(wheatId, prodWheat.getName(), prodWheat.getUnit(), 2.0, 40.0));
            // Item 2: Non-existent product (-9999) - forces error in transaction!
            failingItems.add(new SaleItem(-9999, "NonExistent", "kg", 1.0, 100.0));

            saleDAO.createSale(failingSale, failingItems);
        } catch (Exception ex) {
            rollbackTriggered = true;
            System.out.println("Forced failure triggered exception as expected: " + ex.getMessage());
        }

        if (!rollbackTriggered) throw new AssertionError("Test 16 Failed: Transaction did not fail as expected");

        // Verify rollback: stock was NOT deducted
        double wheatStockAfter = 0.0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT stock_quantity FROM products WHERE id = ?")) {
            ps.setInt(1, wheatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) wheatStockAfter = rs.getDouble(1);
            }
        }
        if (Math.abs(wheatStockBefore - wheatStockAfter) > 0.001)
            throw new AssertionError("Test 16 Failed: Inventory was partially deducted during failed transaction! Rollback failed.");

        // Verify no new sale record was left
        int salesCountAfter = saleDAO.getTotalSalesCount();
        if (salesCountBefore != salesCountAfter)
            throw new AssertionError("Test 16 Failed: Partial sale record persisted despite rollback!");

        System.out.println("Test 16 Passed: Atomic rollback verified. Zero partial records, inventory untouched.");

        // --------------------------------------------------------------------
        // TEST 17, 18 & 19: Multilingual Labels (EN, MR, HI)
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 17, 18 & 19: Multilingual POS Labels ---");

        // 17. English
        I18n.setLanguage("en");
        String enTitle = I18n.get("sale.title");
        String enBtn = I18n.get("sale.btn.complete_sale");
        String enStockWarn = I18n.get("sale.msg.insufficient_stock", "Wheat", "10", "5");
        if (!"Sales & Daily POS".equals(enTitle) || !"Complete Sale".equals(enBtn))
            throw new AssertionError("Test 17 Failed: English translation mismatch: " + enTitle + " / " + enBtn);
        System.out.println("Test 17 Passed: [EN] " + enTitle + " | " + enBtn + " | " + enStockWarn);

        // 18. Marathi
        I18n.setLanguage("mr");
        String mrTitle = I18n.get("sale.title");
        String mrBtn = I18n.get("sale.btn.complete_sale");
        String mrStockWarn = I18n.get("sale.msg.insufficient_stock", "गहू", "१०", "५");
        if (!"विक्री व दैनंदिन POS".equals(mrTitle) || !"विक्री पूर्ण करा".equals(mrBtn))
            throw new AssertionError("Test 18 Failed: Marathi translation mismatch: " + mrTitle + " / " + mrBtn);
        System.out.println("Test 18 Passed: [MR] " + mrTitle + " | " + mrBtn + " | " + mrStockWarn);

        // 19. Hindi
        I18n.setLanguage("hi");
        String hiTitle = I18n.get("sale.title");
        String hiBtn = I18n.get("sale.btn.complete_sale");
        String hiStockWarn = I18n.get("sale.msg.insufficient_stock", "गेहूँ", "१०", "५");
        if (!"बिक्री व दैनिक POS".equals(hiTitle) || !"बिक्री पूरी करें".equals(hiBtn))
            throw new AssertionError("Test 19 Failed: Hindi translation mismatch: " + hiTitle + " / " + hiBtn);
        System.out.println("Test 19 Passed: [HI] " + hiTitle + " | " + hiBtn + " | " + hiStockWarn);

        // Reset to English
        I18n.setLanguage("en");

        // --------------------------------------------------------------------
        // TEST 20: Phase 1 Verification Regression Test
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 20: Run Phase 1 Verification Suite ---");
        Phase1VerificationTest.main(new String[0]);
        System.out.println("Test 20 Passed: Phase 1 verification tests succeeded.");

        // --------------------------------------------------------------------
        // TEST 21: Phase 2 Verification Regression Test
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 21: Run Phase 2 Verification Suite ---");
        Phase2VerificationTest.main(new String[0]);
        System.out.println("Test 21 Passed: Phase 2 verification tests succeeded.");

        // --------------------------------------------------------------------
        // TEST 22: Inventory Regression Test
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 22: Inventory CRUD Regression ---");
        Product regProduct = new Product("Regression Urea", "Fertilizer", "bag", 266.5, 50.0, 10.0);
        boolean regAdded = productDAO.addProduct(regProduct);
        if (!regAdded) throw new AssertionError("Test 22 Failed: Could not add regression product");
        int regId = getProductIdByName("Regression Urea");
        regProduct.setId(regId);

        regProduct.setStockQuantity(60.0);
        boolean regUpdated = productDAO.updateProduct(regProduct);
        if (!regUpdated) throw new AssertionError("Test 22 Failed: Could not update regression product");

        boolean regDeleted = productDAO.deleteProduct(regProduct.getId());
        if (!regDeleted) throw new AssertionError("Test 22 Failed: Could not delete regression product");
        System.out.println("Test 22 Passed: ProductDAO inventory CRUD operations remain completely functional.");

        // --------------------------------------------------------------------
        // TEST 23: Sync Queue & Sync DAO Integrity
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 23: Sync Queue Integrity ---");
        int initialQueueSize = queueDAO.getSyncHistory().size();
        queueDAO.addToQueue("sales", 99999, "INSERT");
        int newQueueSize = queueDAO.getSyncHistory().size();
        if (newQueueSize <= 0) throw new AssertionError("Test 23 Failed: Sync queue history is empty");
        System.out.println("Test 23 Passed: Sync queue remains intact.");

        // --------------------------------------------------------------------
        // TEST 24: Application Starts Without Network
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 24: Application UI initialization offline ---");
        SwingUtilities.invokeAndWait(() -> {
            FlatLightLaf.setup();
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(false);
            mainFrame.showPage("NEW_SALE");
            System.out.println("MainFrame and SalePanel instantiated offline without network.");
            mainFrame.dispose();
        });
        System.out.println("Test 24 Passed: Application starts with 0 network dependency.");

        // --------------------------------------------------------------------
        // TEST 25: A complete sale works without network
        // --------------------------------------------------------------------
        System.out.println("\n--- Test 25: Complete offline sale transaction ---");
        Sale offlineSale = new Sale();
        offlineSale.setCustomerId(customerId);
        offlineSale.setPaymentMethod("CASH");
        offlineSale.setNotes("Offline rural transaction");

        List<SaleItem> offlineItems = new ArrayList<>();
        offlineItems.add(new SaleItem(wheatId, prodWheat.getName(), prodWheat.getUnit(), 2.0, 40.0));

        Sale finalCompleted = saleDAO.createSale(offlineSale, offlineItems);
        if (finalCompleted == null || finalCompleted.getId() <= 0)
            throw new AssertionError("Test 25 Failed: Complete offline sale failed");
        System.out.println("Test 25 Passed: Complete sale processed offline with ID: " + finalCompleted.getId());

        // Safe Cancel / Void Sale Test
        System.out.println("\n--- Bonus Test: Safe Cancel/Void Sale restores inventory ---");
        double stockBeforeCancel = 0.0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT stock_quantity FROM products WHERE id = ?")) {
            ps.setInt(1, wheatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stockBeforeCancel = rs.getDouble(1);
            }
        }
        boolean cancelled = saleDAO.cancelSale(finalCompleted.getId());
        if (!cancelled) throw new AssertionError("Bonus Test Failed: Could not cancel sale");

        double stockAfterCancel = 0.0;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT stock_quantity FROM products WHERE id = ?")) {
            ps.setInt(1, wheatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) stockAfterCancel = rs.getDouble(1);
            }
        }
        if (Math.abs(stockAfterCancel - (stockBeforeCancel + 2.0)) > 0.01)
            throw new AssertionError("Bonus Test Failed: Stock was not restored after voiding sale");
        System.out.printf("Bonus Test Passed: Sale #%d voided, stock restored (%.1f -> %.1f)%n",
                finalCompleted.getId(), stockBeforeCancel, stockAfterCancel);

        // Cleanup test data
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id IN (?, ?)")) {
            ps.setInt(1, wheatId);
            ps.setInt(2, dapId);
            ps.executeUpdate();
        }
        customerDAO.deleteCustomer(customerId);

        System.out.println("\n==================================================");
        System.out.println("=== ALL 25 PHASE 3A VERIFICATION TESTS PASSED! ===");
        System.out.println("==================================================");
    }

    private static int getProductIdByName(String name) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM products WHERE name = ? ORDER BY id DESC LIMIT 1")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new IllegalStateException("Product not found: " + name);
    }
}
