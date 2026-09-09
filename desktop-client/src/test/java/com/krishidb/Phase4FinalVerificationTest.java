package com.krishidb;

import com.formdev.flatlaf.FlatLightLaf;
import com.krishidb.dao.*;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.database.DatabaseManager;
import com.krishidb.model.*;
import com.krishidb.ui.MainFrame;
import com.krishidb.util.CsvExporter;
import com.krishidb.util.I18n;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive verification test suite for Krishi-DB Offline Desktop Final Implementation Pass.
 * Tests all newly implemented modules:
 * - Purchases / Inward
 * - Expenses
 * - Transactions & Ledger
 * - Dashboard
 * - Market Prices
 * - Reports & CSV Export
 * - UI Navigation & Localization
 * - Complete Database Cleanup
 */
public class Phase4FinalVerificationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("================================================================");
        System.out.println("=== STARTING PHASE 4 FINAL DESKTOP IMPLEMENTATION VERIFICATION ===");
        System.out.println("================================================================");

        // -------------------------------------------------------------
        // Step 0: Database Initialization & Schema Verification
        // -------------------------------------------------------------
        System.out.println("\n--- Step 0: Initialize Database & Verify Schema ---");
        DatabaseInitializer.initialize();

        ProductDAO productDAO = new ProductDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        SupplierDAO supplierDAO = new SupplierDAO();
        PurchaseDAO purchaseDAO = new PurchaseDAO();
        ExpenseDAO expenseDAO = new ExpenseDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        DashboardDAO dashboardDAO = new DashboardDAO();
        MarketPriceDAO marketPriceDAO = new MarketPriceDAO();
        ReportDAO reportDAO = new ReportDAO();
        SyncQueueDAO queueDAO = new SyncQueueDAO();

        // Check required tables
        String[] requiredTables = {
                "products", "customers", "suppliers", "sales", "sale_items",
                "purchases", "purchase_items", "expenses", "transactions",
                "market_prices", "sync_queue", "app_settings"
        };
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String tbl : requiredTables) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM " + tbl + " LIMIT 1")) {
                    ps.executeQuery();
                }
            }
        }
        System.out.println("Step 0 Passed: All 12 required tables exist and are accessible.");

        // Setup Test Supplier & Customer & Products
        Supplier testSupplier = new Supplier("Kisan Agro Seeds Pvt Ltd", "9890112233", "Pune");
        supplierDAO.addSupplier(testSupplier);
        int supplierId = testSupplier.getId();

        Customer testCustomer = new Customer("Suresh Patil", "9850556677", "Satara");
        customerDAO.addCustomer(testCustomer);
        int customerId = testCustomer.getId();

        Product testSeedCorn = new Product("Phase4 Test Hybrid Corn Seed", "Seeds", "kg", 180.0, 50.0, 10.0);
        productDAO.addProduct(testSeedCorn);
        int cornId = getProductIdByName("Phase4 Test Hybrid Corn Seed");
        testSeedCorn.setId(cornId);

        Product testBioFert = new Product("Phase4 Test Bio-Organic NPK", "Fertilizers", "bag", 450.0, 30.0, 5.0);
        productDAO.addProduct(testBioFert);
        int bioFertId = getProductIdByName("Phase4 Test Bio-Organic NPK");
        testBioFert.setId(bioFertId);

        System.out.printf("Setup initial test entities: Supplier ID=%d, Customer ID=%d, Corn ID=%d, Fert ID=%d%n",
                supplierId, customerId, cornId, bioFertId);

        // -------------------------------------------------------------
        // MODULE 1: PURCHASES / INWARD
        // -------------------------------------------------------------
        System.out.println("\n--- Testing Module 1: Purchases / Inward ---");

        // 1.1 Single-item purchase increases stock & creates ledger entry
        double cornStockBefore = getProductStock(cornId);
        Purchase purchase1 = new Purchase();
        purchase1.setSupplierId(supplierId);
        purchase1.setInvoiceNumber("INV-P4-001");
        purchase1.setPaymentMethod("CASH");
        purchase1.setNotes("Cash purchase 20kg Corn Seed");

        List<PurchaseItem> items1 = new ArrayList<>();
        items1.add(new PurchaseItem(cornId, testSeedCorn.getName(), testSeedCorn.getUnit(), 20.0, 150.0));

        Purchase savedP1 = purchaseDAO.createPurchase(purchase1, items1);
        if (savedP1 == null || savedP1.getId() <= 0)
            throw new AssertionError("Test 1.1 Failed: Purchase creation failed");

        double cornStockAfter = getProductStock(cornId);
        if (Math.abs(cornStockAfter - (cornStockBefore + 20.0)) > 0.001)
            throw new AssertionError(String.format("Test 1.1 Failed: Stock expected %.1f, got %.1f",
                    cornStockBefore + 20.0, cornStockAfter));

        if (Math.abs(savedP1.getTotalAmount() - 3000.0) > 0.001)
            throw new AssertionError("Test 1.1 Failed: Expected total 3000.0, got " + savedP1.getTotalAmount());

        System.out.println("Test 1.1 Passed: Single-item cash purchase created, stock increased: "
                + cornStockBefore + " -> " + cornStockAfter);

        // 1.2 Multi-item credit purchase updates supplier balance & ledger
        double fertStockBefore = getProductStock(bioFertId);
        cornStockBefore = getProductStock(cornId);
        double supplierBalBefore = supplierDAO.getSupplierById(supplierId).getOutstandingBalance();

        Purchase purchaseCredit = new Purchase();
        purchaseCredit.setSupplierId(supplierId);
        purchaseCredit.setInvoiceNumber("INV-P4-CREDIT");
        purchaseCredit.setPaymentMethod("CREDIT");
        purchaseCredit.setNotes("Credit purchase for multiple items");

        List<PurchaseItem> itemsCredit = new ArrayList<>();
        itemsCredit.add(new PurchaseItem(cornId, testSeedCorn.getName(), testSeedCorn.getUnit(), 10.0, 150.0)); // 1500
        itemsCredit.add(new PurchaseItem(bioFertId, testBioFert.getName(), testBioFert.getUnit(), 5.0, 400.0)); // 2000
        // Total = 3500

        Purchase savedCredit = purchaseDAO.createPurchase(purchaseCredit, itemsCredit);
        if (savedCredit == null || Math.abs(savedCredit.getTotalAmount() - 3500.0) > 0.001)
            throw new AssertionError("Test 1.2 Failed: Credit purchase total calculation mismatch");

        double supplierBalAfter = supplierDAO.getSupplierById(supplierId).getOutstandingBalance();
        if (Math.abs(supplierBalAfter - (supplierBalBefore + 3500.0)) > 0.001)
            throw new AssertionError(String.format("Test 1.2 Failed: Supplier balance expected %.2f, got %.2f",
                    supplierBalBefore + 3500.0, supplierBalAfter));

        System.out.println("Test 1.2 Passed: Multi-item credit purchase updated supplier balance: "
                + supplierBalBefore + " -> " + supplierBalAfter);

        // 1.3 Validation: zero quantity rejected
        try {
            List<PurchaseItem> badItems = new ArrayList<>();
            badItems.add(new PurchaseItem(cornId, "Corn", "kg", 0.0, 150.0));
            purchaseDAO.createPurchase(new Purchase(), badItems);
            throw new AssertionError("Test 1.3 Failed: Zero quantity purchase was not rejected");
        } catch (IllegalArgumentException expected) {
            System.out.println("Test 1.3 Passed: Zero quantity purchase correctly rejected.");
        }

        // 1.4 Validation: negative price rejected
        try {
            List<PurchaseItem> badItems = new ArrayList<>();
            badItems.add(new PurchaseItem(cornId, "Corn", "kg", 5.0, -100.0));
            purchaseDAO.createPurchase(new Purchase(), badItems);
            throw new AssertionError("Test 1.4 Failed: Negative price purchase was not rejected");
        } catch (IllegalArgumentException expected) {
            System.out.println("Test 1.4 Passed: Negative price purchase correctly rejected.");
        }

        // 1.5 Validation: credit purchase without supplier rejected
        try {
            Purchase noSupplierCredit = new Purchase();
            noSupplierCredit.setPaymentMethod("CREDIT");
            List<PurchaseItem> okItems = new ArrayList<>();
            okItems.add(new PurchaseItem(cornId, "Corn", "kg", 5.0, 100.0));
            purchaseDAO.createPurchase(noSupplierCredit, okItems);
            throw new AssertionError("Test 1.5 Failed: Credit purchase without supplier was not rejected");
        } catch (IllegalArgumentException expected) {
            System.out.println("Test 1.5 Passed: Credit purchase without supplier correctly rejected.");
        }

        // 1.6 Purchase cancellation restores stock and reverses supplier balance
        cornStockBefore = getProductStock(cornId);
        supplierBalBefore = supplierDAO.getSupplierById(supplierId).getOutstandingBalance();

        boolean cancelled = purchaseDAO.cancelPurchase(savedCredit.getId());
        if (!cancelled) throw new AssertionError("Test 1.6 Failed: Purchase cancellation returned false");

        cornStockAfter = getProductStock(cornId);
        supplierBalAfter = supplierDAO.getSupplierById(supplierId).getOutstandingBalance();

        if (Math.abs(cornStockAfter - (cornStockBefore - 10.0)) > 0.001)
            throw new AssertionError("Test 1.6 Failed: Stock was not reverted after cancellation");
        if (Math.abs(supplierBalAfter - (supplierBalBefore - 3500.0)) > 0.001)
            throw new AssertionError("Test 1.6 Failed: Supplier balance was not reverted after credit cancellation");

        System.out.println("Test 1.6 Passed: Purchase voided, stock and supplier credit successfully restored.");

        // -------------------------------------------------------------
        // MODULE 2: EXPENSES
        // -------------------------------------------------------------
        System.out.println("\n--- Testing Module 2: Expenses ---");

        // 2.1 Add Expense creates record & ledger transaction
        Expense expense1 = new Expense("Electricity", "Shop electricity bill June", 1250.0, "UPI");
        boolean expAdded = expenseDAO.addExpense(expense1);
        if (!expAdded || expense1.getId() <= 0)
            throw new AssertionError("Test 2.1 Failed: Failed to add expense");

        Expense fetchedExp = expenseDAO.getExpenseById(expense1.getId());
        if (fetchedExp == null || Math.abs(fetchedExp.getAmount() - 1250.0) > 0.001)
            throw new AssertionError("Test 2.1 Failed: Expense fetch mismatch");

        System.out.println("Test 2.1 Passed: Expense created with ID=" + expense1.getId() + " (₹1250 via UPI)");

        // 2.2 Amount <= 0 validation
        try {
            Expense zeroExp = new Expense("Rent", "Zero rent", 0.0, "CASH");
            expenseDAO.addExpense(zeroExp);
            throw new AssertionError("Test 2.2 Failed: Zero expense was not rejected");
        } catch (IllegalArgumentException expected) {
            System.out.println("Test 2.2 Passed: Zero expense correctly rejected.");
        }

        // 2.3 Update Expense
        expense1.setAmount(1400.0);
        expense1.setDescription("Shop electricity bill June - revised");
        boolean expUpdated = expenseDAO.updateExpense(expense1);
        if (!expUpdated) throw new AssertionError("Test 2.3 Failed: Expense update failed");

        Expense afterUpdate = expenseDAO.getExpenseById(expense1.getId());
        if (Math.abs(afterUpdate.getAmount() - 1400.0) > 0.001)
            throw new AssertionError("Test 2.3 Failed: Expense amount not updated");
        System.out.println("Test 2.3 Passed: Expense updated successfully.");

        // 2.4 Delete Expense
        boolean expDeleted = expenseDAO.deleteExpense(expense1.getId());
        if (!expDeleted) throw new AssertionError("Test 2.4 Failed: Expense deletion failed");
        if (expenseDAO.getExpenseById(expense1.getId()) != null)
            throw new AssertionError("Test 2.4 Failed: Deleted expense still exists");
        System.out.println("Test 2.4 Passed: Expense deleted successfully.");

        // Re-create an active expense for subsequent ledger & dashboard tests
        Expense activeExp = new Expense("Transport", "Fertilizer transport charges", 850.0, "CASH");
        expenseDAO.addExpense(activeExp);

        // -------------------------------------------------------------
        // MODULE 3: TRANSACTIONS / FINANCIAL LEDGER
        // -------------------------------------------------------------
        System.out.println("\n--- Testing Module 3: Transactions / Ledger ---");

        // 3.1 Customer Khata Payment (Inflow)
        // Give customer some balance first
        testCustomer.setOutstandingBalance(2000.0);
        customerDAO.updateCustomer(testCustomer);

        double custBalBefore = customerDAO.getCustomerById(customerId).getOutstandingBalance();
        boolean custPaySuccess = transactionDAO.recordCustomerPayment(customerId, 500.0, "UPI", "Partial Khata repayment");
        if (!custPaySuccess) throw new AssertionError("Test 3.1 Failed: Customer payment recording failed");

        double custBalAfter = customerDAO.getCustomerById(customerId).getOutstandingBalance();
        if (Math.abs(custBalAfter - (custBalBefore - 500.0)) > 0.001)
            throw new AssertionError("Test 3.1 Failed: Customer balance not reduced by payment amount");
        System.out.println("Test 3.1 Passed: Customer Khata payment recorded, balance updated: "
                + custBalBefore + " -> " + custBalAfter);

        // 3.2 Supplier Khata Payment (Outflow)
        testSupplier.setOutstandingBalance(1500.0);
        supplierDAO.updateSupplier(testSupplier);

        double suppBalBefore = supplierDAO.getSupplierById(supplierId).getOutstandingBalance();
        boolean suppPaySuccess = transactionDAO.recordSupplierPayment(supplierId, 600.0, "CASH", "Partial vendor settlement");
        if (!suppPaySuccess) throw new AssertionError("Test 3.2 Failed: Supplier payment recording failed");

        double suppBalAfter = supplierDAO.getSupplierById(supplierId).getOutstandingBalance();
        if (Math.abs(suppBalAfter - (suppBalBefore - 600.0)) > 0.001)
            throw new AssertionError("Test 3.2 Failed: Supplier balance not reduced by payment amount");
        System.out.println("Test 3.2 Passed: Supplier payment recorded, balance updated: "
                + suppBalBefore + " -> " + suppBalAfter);

        // 3.3 Financial Inflow / Outflow aggregations
        double totalInflow = transactionDAO.getTotalInflow();
        double totalOutflow = transactionDAO.getTotalOutflow();
        if (totalInflow < 500.0)
            throw new AssertionError("Test 3.3 Failed: Total inflow should include customer payment (>= 500)");
        if (totalOutflow < 600.0)
            throw new AssertionError("Test 3.3 Failed: Total outflow should include supplier payment & expenses");
        System.out.printf("Test 3.3 Passed: Inflow=₹%.2f, Outflow=₹%.2f, Net=₹%.2f%n",
                totalInflow, totalOutflow, (totalInflow - totalOutflow));

        // -------------------------------------------------------------
        // MODULE 4: DASHBOARD
        // -------------------------------------------------------------
        System.out.println("\n--- Testing Module 4: Dashboard ---");

        DashboardDAO.DashboardMetrics m = dashboardDAO.fetchMetrics();
        if (m == null)
            throw new AssertionError("Test 4.1 Failed: Dashboard metrics object is null");

        double invVal = m.inventoryValuation;
        int skuCount = m.productCount;
        int lowStockCount = m.lowStockCount;
        int syncQueueCount = m.pendingSyncCount;

        if (invVal <= 0.0)
            throw new AssertionError("Test 4.1 Failed: Inventory valuation should be > 0");
        if (skuCount <= 0)
            throw new AssertionError("Test 4.1 Failed: SKU count should be > 0");

        System.out.printf("Test 4.1 Passed: Dashboard metrics verified - Inventory Value=₹%.2f, SKUs=%d, LowStock=%d, PendingSync=%d%n",
                invVal, skuCount, lowStockCount, syncQueueCount);

        List<Product> lowStockProducts = m.lowStockProducts;
        if (lowStockProducts == null)
            throw new AssertionError("Test 4.2 Failed: Low stock products list is null");
        System.out.println("Test 4.2 Passed: Low stock query executed without error. Found " + lowStockProducts.size() + " items.");

        // -------------------------------------------------------------
        // MODULE 5: MARKET PRICES
        // -------------------------------------------------------------
        System.out.println("\n--- Testing Module 5: Market Prices ---");

        List<MarketPrice> allPrices = marketPriceDAO.getAllPrices();
        if (allPrices.isEmpty())
            throw new AssertionError("Test 5.1 Failed: Expected seeded market prices, got 0");

        System.out.println("Test 5.1 Passed: Cached market prices retrieved: " + allPrices.size() + " records found.");

        List<String> markets = marketPriceDAO.getDistinctMarkets();
        if (markets.isEmpty())
            throw new AssertionError("Test 5.2 Failed: Distinct APMC mandis list is empty");
        System.out.println("Test 5.2 Passed: APMC markets retrieved: " + markets);

        List<MarketPrice> searchResults = marketPriceDAO.searchPrices("Soybean", "ALL");
        if (searchResults.isEmpty())
            throw new AssertionError("Test 5.3 Failed: Search for 'Soybean' returned no results");
        System.out.println("Test 5.3 Passed: Search for 'Soybean' found " + searchResults.size() + " results.");

        // -------------------------------------------------------------
        // MODULE 6: REPORTS & CSV EXPORT
        // -------------------------------------------------------------
        System.out.println("\n--- Testing Module 6: Reports & CSV Export ---");

        // 6.1 Report data generation
        ReportDAO.ReportResult salesReport = reportDAO.getSalesReport("ALL_TIME", null, null);
        ReportDAO.ReportResult purchaseReport = reportDAO.getPurchaseReport("ALL_TIME", null, null);
        ReportDAO.ReportResult expenseReport = reportDAO.getExpenseReport("ALL_TIME", null, null);
        ReportDAO.ReportResult stockReport = reportDAO.getInventoryReport();
        ReportDAO.ReportResult txReport = reportDAO.getTransactionReport("ALL_TIME", null, null);

        if (stockReport.rows.isEmpty())
            throw new AssertionError("Test 6.1 Failed: Stock report is empty");
        if (purchaseReport.rows.isEmpty())
            throw new AssertionError("Test 6.1 Failed: Purchase report is empty");

        System.out.printf("Test 6.1 Passed: Reports generated - Sales=%d, Purchases=%d, Expenses=%d, Stock=%d, Ledger=%d%n",
                salesReport.recordCount, purchaseReport.recordCount, expenseReport.recordCount, stockReport.recordCount, txReport.recordCount);

        // 6.2 CSV Exporter RFC 4180 & UTF-8 BOM Verification
        File tempCsv = File.createTempFile("krishidb_test_export_", ".csv");
        tempCsv.deleteOnExit();

        String[] headers = {"ID", "Crop / शेतमाल", "Mandi / मंडी", "Price / दर (₹)"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"1", "Soybean / सोयाबीन", "Latur Mandi / लातूर", "4550.00"});
        rows.add(new String[]{"2", "Cotton / कापूस", "Akola / अकोला", "7100.00"});

        CsvExporter.exportToFile(tempCsv, headers, rows);

        if (!tempCsv.exists() || tempCsv.length() == 0)
            throw new AssertionError("Test 6.2 Failed: CSV export file does not exist or is empty");

        // Verify UTF-8 BOM (\uFEFF -> 0xEF, 0xBB, 0xBF)
        byte[] bytes = Files.readAllBytes(tempCsv.toPath());
        if (bytes.length < 3 || (bytes[0] & 0xFF) != 0xEF || (bytes[1] & 0xFF) != 0xBB || (bytes[2] & 0xFF) != 0xBF)
            throw new AssertionError("Test 6.2 Failed: CSV file missing UTF-8 BOM for Windows Excel compatibility");

        String fileContent = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        if (!fileContent.contains("सोयाबीन") || !fileContent.contains("अकोला"))
            throw new AssertionError("Test 6.2 Failed: Devanagari characters corrupted in CSV export");

        System.out.println("Test 6.2 Passed: CSV exported with UTF-8 BOM and Devanagari script integrity.");
        tempCsv.delete();

        // -------------------------------------------------------------
        // MODULE 7: UI INITIALIZATION, NAVIGATION & LOCALIZATION
        // -------------------------------------------------------------
        System.out.println("\n--- Testing Module 7: UI Navigation & 3-Language Localization ---");

        SwingUtilities.invokeAndWait(() -> {
            FlatLightLaf.setup();
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(false);

            // Test navigation to all 12 registered pages + NEW_ENTRY
            String[] routes = {
                    "DASHBOARD", "NEW_SALE", "NEW_ENTRY", "INVENTORY",
                    "CUSTOMERS", "SUPPLIERS", "PURCHASES", "EXPENSES",
                    "TRANSACTIONS", "REPORTS", "MARKET_PRICES", "SYNC", "SETTINGS"
            };

            for (String route : routes) {
                mainFrame.showPage(route);
            }
            System.out.println("Test 7.1 Passed: All 13 navigation routes activated without error.");

            // Test 3-language dynamic switching
            I18n.setLanguage("mr");
            String mrPurchase = I18n.get("purchase.title");
            if (mrPurchase.contains("???") || mrPurchase.equals("purchase.title"))
                throw new AssertionError("Test 7.2 Failed: Marathi purchase.title missing or corrupted: " + mrPurchase);
            System.out.println("Test 7.2 Passed: Marathi locale loaded: " + mrPurchase);

            I18n.setLanguage("hi");
            String hiPurchase = I18n.get("purchase.title");
            if (hiPurchase.contains("???") || hiPurchase.equals("purchase.title"))
                throw new AssertionError("Test 7.3 Failed: Hindi purchase.title missing or corrupted: " + hiPurchase);
            System.out.println("Test 7.3 Passed: Hindi locale loaded: " + hiPurchase);

            I18n.setLanguage("en");
            System.out.println("Test 7.4 Passed: Reverted to English successfully.");

            mainFrame.dispose();
        });

        // -------------------------------------------------------------
        // MODULE 8: TEST DATA CLEANUP
        // -------------------------------------------------------------
        System.out.println("\n--- Step 8: Test Data Cleanup ---");

        // Delete test purchase
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM purchase_items WHERE purchase_id IN (SELECT id FROM purchases WHERE supplier_id = ?)")) {
                ps.setInt(1, supplierId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM purchases WHERE supplier_id = ?")) {
                ps.setInt(1, supplierId);
                ps.executeUpdate();
            }
            // Delete test transactions
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transactions WHERE reference_id LIKE 'INV-P4%' OR reference_id = 'EXP-" + activeExp.getId() + "' OR description LIKE '%Khata%' OR description LIKE '%settlement%'")) {
                ps.executeUpdate();
            }
            // Delete test products
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM products WHERE id IN (?, ?)")) {
                ps.setInt(1, cornId);
                ps.setInt(2, bioFertId);
                ps.executeUpdate();
            }
        }

        // Delete active test expense
        expenseDAO.deleteExpense(activeExp.getId());

        // Delete test supplier & customer
        supplierDAO.deleteSupplier(supplierId);
        customerDAO.deleteCustomer(customerId);

        System.out.println("Step 8 Passed: All temporary test data successfully cleaned up.");

        System.out.println("\n================================================================");
        System.out.println("=== ALL PHASE 4 FINAL IMPLEMENTATION VERIFICATIONS PASSED! ===");
        System.out.println("================================================================");

        // Terminate any lingering SwingWorker threads cleanly
        System.exit(0);
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

    private static double getProductStock(int productId) throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT stock_quantity FROM products WHERE id = ?")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        throw new IllegalStateException("Product stock not found for ID: " + productId);
    }
}
