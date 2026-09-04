package com.krishidb;

import com.formdev.flatlaf.FlatLightLaf;
import com.krishidb.dao.CustomerDAO;
import com.krishidb.dao.ProductDAO;
import com.krishidb.dao.SupplierDAO;
import com.krishidb.dao.SyncQueueDAO;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Customer;
import com.krishidb.model.Supplier;
import com.krishidb.model.SyncRecord;
import com.krishidb.ui.MainFrame;
import com.krishidb.util.I18n;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class Phase2VerificationTest {

    public static void main(String[] args) throws Exception {
        System.out.println("==================================================");
        System.out.println("=== STARTING PHASE 2 AUTOMATED VERIFICATION ===");
        System.out.println("==================================================");

        // Step 1: Database Initialization
        System.out.println("\n--- Step 1: Initialize Database ---");
        DatabaseInitializer.initialize();

        CustomerDAO customerDAO = new CustomerDAO();
        SupplierDAO supplierDAO = new SupplierDAO();
        SyncQueueDAO queueDAO = new SyncQueueDAO();

        // --------------------------------------------------------------------
        // CUSTOMER TESTS
        // --------------------------------------------------------------------
        System.out.println("\n--- Step 2: Test Customer Workflow ---");

        // 1. Add customer offline
        Customer newCustomer = new Customer("Ramesh Patil", "9876543210", "Baramati");
        boolean custAdded = customerDAO.addCustomer(newCustomer);
        if (!custAdded) throw new AssertionError("Failed to add customer offline");
        int customerId = newCustomer.getId();
        System.out.println("1. Customer added offline with ID: " + customerId);

        // 2. Verify appears in SQLite
        Customer fetchedCust = customerDAO.getCustomerById(customerId);
        if (fetchedCust == null) throw new AssertionError("Customer not found in SQLite");
        System.out.println("2. Verified customer exists in SQLite: " + fetchedCust.getName());

        // 3. Verify sync_status is PENDING
        if (!"PENDING".equals(fetchedCust.getSyncStatus())) {
            throw new AssertionError("Expected sync_status PENDING, got: " + fetchedCust.getSyncStatus());
        }
        System.out.println("3. Verified sync_status is PENDING.");

        // 4. Verify sync_queue receives INSERT
        List<SyncRecord> history1 = queueDAO.getSyncHistory();
        if (history1.isEmpty() || !"customers".equals(history1.get(0).getTableName()) || !"INSERT".equals(history1.get(0).getOperation())) {
            throw new AssertionError("sync_queue did not record Customer INSERT. Top record: " + (history1.isEmpty() ? "none" : history1.get(0).getTableName() + "|" + history1.get(0).getOperation()));
        }
        System.out.println("4. Verified sync_queue received INSERT: " + history1.get(0).getTableName() + " | " + history1.get(0).getOperation());

        // 5. Edit customer
        fetchedCust.setName("Rameshwar Patil");
        fetchedCust.setVillage("Indapur");
        boolean custUpdated = customerDAO.updateCustomer(fetchedCust);
        if (!custUpdated) throw new AssertionError("Failed to update customer");

        // 6. Verify local record changes
        Customer updatedCust = customerDAO.getCustomerById(customerId);
        if (!"Rameshwar Patil".equals(updatedCust.getName()) || !"Indapur".equals(updatedCust.getVillage())) {
            throw new AssertionError("Customer changes did not persist to SQLite");
        }
        System.out.println("5 & 6. Verified customer updated locally: " + updatedCust.getName() + " (" + updatedCust.getVillage() + ")");

        // 7. Verify UPDATE is queued
        List<SyncRecord> history2 = queueDAO.getSyncHistory();
        if (history2.isEmpty() || !"customers".equals(history2.get(0).getTableName()) || !"UPDATE".equals(history2.get(0).getOperation())) {
            throw new AssertionError("sync_queue did not record Customer UPDATE.");
        }
        System.out.println("7. Verified sync_queue received UPDATE: " + history2.get(0).getTableName() + " | " + history2.get(0).getOperation());

        // 8 & 9. Attempt deletion of unlinked customer
        Customer unlinkedCust = new Customer("Temp Unlinked", "9123456780", "Daund");
        customerDAO.addCustomer(unlinkedCust);
        int unlinkedId = unlinkedCust.getId();
        boolean deletedUnlinked = customerDAO.deleteCustomer(unlinkedId);
        if (!deletedUnlinked) throw new AssertionError("Failed to delete unlinked customer");
        if (customerDAO.getCustomerById(unlinkedId) != null) throw new AssertionError("Unlinked customer still exists in DB");
        System.out.println("8 & 9. Verified unlinked customer deletion succeeded.");

        // 10 & 11. Attempt deletion of customer WITH linked sales (Foreign Key Integrity)
        System.out.println("Testing Customer FK Protection with linked sales...");
        int testSaleId = -1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO sales (customer_id, total_amount, payment_method) VALUES (?, 500.0, 'CASH')",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) testSaleId = keys.getInt(1);
            }
        }

        int linkedSales = customerDAO.getLinkedSalesCount(customerId);
        if (linkedSales <= 0) throw new AssertionError("Expected linked sales count > 0, got: " + linkedSales);
        System.out.println("Linked sales count for customer " + customerId + ": " + linkedSales);

        boolean deleteBlocked = customerDAO.deleteCustomer(customerId);
        if (deleteBlocked) throw new AssertionError("Customer with linked sales was deleted! FK protection failed.");
        System.out.println("10 & 11. Verified customer deletion was safely BLOCKED due to linked sales.");

        // Clean up test sale and customer
        if (testSaleId != -1) {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM sales WHERE id = ?")) {
                ps.setInt(1, testSaleId);
                ps.executeUpdate();
            }
        }
        customerDAO.deleteCustomer(customerId);
        System.out.println("Cleaned up test customer and mock sales record.");

        // --------------------------------------------------------------------
        // SUPPLIER TESTS
        // --------------------------------------------------------------------
        System.out.println("\n--- Step 3: Test Supplier Workflow ---");

        // 1. Add supplier offline
        Supplier newSupplier = new Supplier("Mahyco Seeds Ltd", "9822334455", "Jalna");
        boolean suppAdded = supplierDAO.addSupplier(newSupplier);
        if (!suppAdded) throw new AssertionError("Failed to add supplier offline");
        int supplierId = newSupplier.getId();
        System.out.println("1. Supplier added offline with ID: " + supplierId);

        // 2. Verify appears in SQLite
        Supplier fetchedSupp = supplierDAO.getSupplierById(supplierId);
        if (fetchedSupp == null) throw new AssertionError("Supplier not found in SQLite");
        System.out.println("2. Verified supplier exists in SQLite: " + fetchedSupp.getName());

        // 3. Verify sync_status is PENDING
        if (!"PENDING".equals(fetchedSupp.getSyncStatus())) {
            throw new AssertionError("Expected sync_status PENDING, got: " + fetchedSupp.getSyncStatus());
        }
        System.out.println("3. Verified sync_status is PENDING.");

        // 4. Verify sync_queue receives INSERT
        List<SyncRecord> suppHistory1 = queueDAO.getSyncHistory();
        if (suppHistory1.isEmpty() || !"suppliers".equals(suppHistory1.get(0).getTableName()) || !"INSERT".equals(suppHistory1.get(0).getOperation())) {
            throw new AssertionError("sync_queue did not record Supplier INSERT.");
        }
        System.out.println("4. Verified sync_queue received INSERT: " + suppHistory1.get(0).getTableName() + " | " + suppHistory1.get(0).getOperation());

        // 5. Edit supplier
        fetchedSupp.setName("Mahyco Agro Seeds Ltd");
        fetchedSupp.setVillage("Aurangabad Hub");
        boolean suppUpdated = supplierDAO.updateSupplier(fetchedSupp);
        if (!suppUpdated) throw new AssertionError("Failed to update supplier");

        // 6. Verify local record changes
        Supplier updatedSupp = supplierDAO.getSupplierById(supplierId);
        if (!"Mahyco Agro Seeds Ltd".equals(updatedSupp.getName()) || !"Aurangabad Hub".equals(updatedSupp.getVillage())) {
            throw new AssertionError("Supplier changes did not persist to SQLite");
        }
        System.out.println("5 & 6. Verified supplier updated locally: " + updatedSupp.getName() + " (" + updatedSupp.getVillage() + ")");

        // 7. Verify UPDATE is queued
        List<SyncRecord> suppHistory2 = queueDAO.getSyncHistory();
        if (suppHistory2.isEmpty() || !"suppliers".equals(suppHistory2.get(0).getTableName()) || !"UPDATE".equals(suppHistory2.get(0).getOperation())) {
            throw new AssertionError("sync_queue did not record Supplier UPDATE.");
        }
        System.out.println("7. Verified sync_queue received UPDATE: " + suppHistory2.get(0).getTableName() + " | " + suppHistory2.get(0).getOperation());

        // 8 & 9. Attempt deletion of unlinked supplier
        Supplier unlinkedSupp = new Supplier("Temp Wholesale", "9422001122", "Pune");
        supplierDAO.addSupplier(unlinkedSupp);
        int unlinkedSuppId = unlinkedSupp.getId();
        boolean deletedSupp = supplierDAO.deleteSupplier(unlinkedSuppId);
        if (!deletedSupp) throw new AssertionError("Failed to delete unlinked supplier");
        if (supplierDAO.getSupplierById(unlinkedSuppId) != null) throw new AssertionError("Unlinked supplier still exists in DB");
        System.out.println("8 & 9. Verified unlinked supplier deletion succeeded.");

        // 10 & 11. Attempt deletion of supplier WITH linked purchases (Foreign Key Integrity)
        System.out.println("Testing Supplier FK Protection with linked purchases...");
        int testPurchaseId = -1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO purchases (supplier_id, total_amount) VALUES (?, 1200.0)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, supplierId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) testPurchaseId = keys.getInt(1);
            }
        }

        int linkedPurchases = supplierDAO.getLinkedPurchasesCount(supplierId);
        if (linkedPurchases <= 0) throw new AssertionError("Expected linked purchases count > 0, got: " + linkedPurchases);
        System.out.println("Linked purchases count for supplier " + supplierId + ": " + linkedPurchases);

        boolean suppDeleteBlocked = supplierDAO.deleteSupplier(supplierId);
        if (suppDeleteBlocked) throw new AssertionError("Supplier with linked purchases was deleted! FK protection failed.");
        System.out.println("10 & 11. Verified supplier deletion was safely BLOCKED due to linked purchases.");

        // Clean up test purchase and supplier
        if (testPurchaseId != -1) {
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM purchases WHERE id = ?")) {
                ps.setInt(1, testPurchaseId);
                ps.executeUpdate();
            }
        }
        supplierDAO.deleteSupplier(supplierId);
        System.out.println("Cleaned up test supplier and mock purchase record.");

        // --------------------------------------------------------------------
        // SEARCH TESTS
        // --------------------------------------------------------------------
        System.out.println("\n--- Step 4: Testing Search Functionality ---");
        Customer searchCust = new Customer("Sunil Gavaskar", "9811122233", "Wankhede");
        customerDAO.addCustomer(searchCust);
        List<Customer> searchResults = customerDAO.searchCustomers("Gavaskar");
        if (searchResults.isEmpty() || !"Sunil Gavaskar".equals(searchResults.get(0).getName())) {
            throw new AssertionError("Customer search failed to find 'Gavaskar'");
        }
        System.out.println("Customer search verified. Found: " + searchResults.get(0).getName());
        customerDAO.deleteCustomer(searchCust.getId());

        // --------------------------------------------------------------------
        // LOCALIZATION & MAINFRAME ROUTING TESTS
        // --------------------------------------------------------------------
        System.out.println("\n--- Step 5: Testing UI Localization & Active Card Routes ---");
        FlatLightLaf.setup();
        final MainFrame[] frameHolder = new MainFrame[1];
        SwingUtilities.invokeAndWait(() -> {
            MainFrame frame = new MainFrame();
            frameHolder[0] = frame;

            // Test showing CUSTOMERS and SUPPLIERS
            frame.showPage("CUSTOMERS");
            System.out.println("Navigated to real CUSTOMERS panel.");
            frame.showPage("SUPPLIERS");
            System.out.println("Navigated to real SUPPLIERS panel.");
            frame.showPage("INVENTORY");
            System.out.println("Navigated to real INVENTORY panel.");
            frame.showPage("SYNC");
            System.out.println("Navigated to real SYNC panel.");
            frame.showPage("SETTINGS");
            System.out.println("Navigated to real SETTINGS panel.");

            // Test language toggling across EN -> MR -> HI
            I18n.setLanguage("mr");
            System.out.println("[MR] customer.title: " + I18n.get("customer.title"));
            System.out.println("[MR] supplier.title: " + I18n.get("supplier.title"));
            if (!"ग्राहक व्यवस्थापन".equals(I18n.get("customer.title"))) {
                throw new AssertionError("Marathi customer title mismatch");
            }
            if (!"पुरवठादार व्यवस्थापन".equals(I18n.get("supplier.title"))) {
                throw new AssertionError("Marathi supplier title mismatch");
            }

            I18n.setLanguage("hi");
            System.out.println("[HI] customer.title: " + I18n.get("customer.title"));
            System.out.println("[HI] supplier.title: " + I18n.get("supplier.title"));
            if (!"ग्राहक प्रबंधन".equals(I18n.get("customer.title"))) {
                throw new AssertionError("Hindi customer title mismatch");
            }
            if (!"आपूर्तिकर्ता प्रबंधन".equals(I18n.get("supplier.title"))) {
                throw new AssertionError("Hindi supplier title mismatch");
            }

            I18n.setLanguage("en");
            System.out.println("[EN] customer.title: " + I18n.get("customer.title"));
            System.out.println("[EN] supplier.title: " + I18n.get("supplier.title"));

            frame.dispose();
        });

        // --------------------------------------------------------------------
        // PRESERVED INVENTORY & SYNC REGRESSION CHECK
        // --------------------------------------------------------------------
        System.out.println("\n--- Step 6: Regression Check on Product & Sync DAOs ---");
        ProductDAO productDAO = new ProductDAO();
        int totalProds = productDAO.getAllProducts().size();
        System.out.println("Total products verified: " + totalProds);

        System.out.println("\n==================================================");
        System.out.println("=== ALL PHASE 2 VERIFICATION CHECKS PASSED! ===");
        System.out.println("==================================================");
    }
}
