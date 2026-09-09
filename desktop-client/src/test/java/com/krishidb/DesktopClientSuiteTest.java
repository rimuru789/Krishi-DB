package com.krishidb;

import com.krishidb.dao.*;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.database.DatabaseManager;
import com.krishidb.model.*;
import com.krishidb.util.CsvExporter;
import com.krishidb.util.I18n;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DesktopClientSuiteTest {

    @BeforeAll
    public static void setup() {
        DatabaseInitializer.initialize();
    }

    @Test
    @Order(1)
    public void testDatabaseSchemaAndTables() throws Exception {
        String[] tables = {
                "products", "customers", "suppliers", "sales", "sale_items",
                "purchases", "purchase_items", "expenses", "transactions",
                "market_prices", "sync_queue", "app_settings"
        };
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String table : tables) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM " + table + " LIMIT 1")) {
                    assertDoesNotThrow(() -> { ps.executeQuery(); }, "Table should be queryable: " + table);
                }
            }
        }
    }

    @Test
    @Order(2)
    public void testPurchasesStockAndLedger() throws Exception {
        SupplierDAO supplierDAO = new SupplierDAO();
        ProductDAO productDAO = new ProductDAO();
        PurchaseDAO purchaseDAO = new PurchaseDAO();

        Supplier supplier = new Supplier("JUnit Supplier " + System.currentTimeMillis(), "9890989098", "Nashik");
        assertTrue(supplierDAO.addSupplier(supplier));
        int supplierId = supplier.getId();

        Product product = new Product("JUnit Test Seeds " + System.currentTimeMillis(), "Seeds", "kg", 200.0, 50.0, 10.0);
        assertTrue(productDAO.addProduct(product));
        int productId = getLatestProductId();

        // 1. Single-item cash purchase increases stock
        Purchase pCash = new Purchase();
        pCash.setSupplierId(supplierId);
        pCash.setInvoiceNumber("INV-CASH-TEST");
        pCash.setPaymentMethod("CASH");

        List<PurchaseItem> items = new ArrayList<>();
        items.add(new PurchaseItem(productId, product.getName(), product.getUnit(), 25.0, 180.0));

        Purchase savedCash = purchaseDAO.createPurchase(pCash, items);
        assertNotNull(savedCash);
        assertEquals(4500.0, savedCash.getTotalAmount(), 0.001);

        Product updatedProd = productDAO.getProductById(productId);
        assertNotNull(updatedProd);
        assertEquals(75.0, updatedProd.getStockQuantity(), 0.001);

        // 2. Credit purchase increases supplier balance
        Purchase pCredit = new Purchase();
        pCredit.setSupplierId(supplierId);
        pCredit.setInvoiceNumber("INV-CREDIT-TEST");
        pCredit.setPaymentMethod("CREDIT");

        List<PurchaseItem> itemsCredit = new ArrayList<>();
        itemsCredit.add(new PurchaseItem(productId, product.getName(), product.getUnit(), 10.0, 180.0));

        Purchase savedCredit = purchaseDAO.createPurchase(pCredit, itemsCredit);
        assertNotNull(savedCredit);

        Supplier updatedSupplier = supplierDAO.getSupplierById(supplierId);
        assertEquals(1800.0, updatedSupplier.getOutstandingBalance(), 0.001);

        // 3. Purchase cancellation reverts stock and supplier balance
        assertTrue(purchaseDAO.cancelPurchase(savedCredit.getId()));

        Supplier afterCancelSupplier = supplierDAO.getSupplierById(supplierId);
        assertEquals(0.0, afterCancelSupplier.getOutstandingBalance(), 0.001);

        Product afterCancelProd = productDAO.getProductById(productId);
        assertEquals(75.0, afterCancelProd.getStockQuantity(), 0.001);

        // Cleanup
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM purchase_items WHERE purchase_id IN (SELECT id FROM purchases WHERE supplier_id = ?)")) {
                ps.setInt(1, supplierId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM purchases WHERE supplier_id = ?")) {
                ps.setInt(1, supplierId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transactions WHERE reference_id LIKE 'INV-%'")) {
                ps.executeUpdate();
            }
        }
        productDAO.deleteProduct(productId);
        supplierDAO.deleteSupplier(supplierId);
    }

    @Test
    @Order(3)
    public void testExpensesCrudAndLedger() throws Exception {
        ExpenseDAO expenseDAO = new ExpenseDAO();

        Expense exp = new Expense("Rent", "Shop godown rent", 3500.0, "UPI");
        assertTrue(expenseDAO.addExpense(exp));
        assertTrue(exp.getId() > 0);

        Expense fetched = expenseDAO.getExpenseById(exp.getId());
        assertNotNull(fetched);
        assertEquals(3500.0, fetched.getAmount(), 0.001);

        exp.setAmount(4000.0);
        assertTrue(expenseDAO.updateExpense(exp));

        Expense afterUpdate = expenseDAO.getExpenseById(exp.getId());
        assertEquals(4000.0, afterUpdate.getAmount(), 0.001);

        assertTrue(expenseDAO.deleteExpense(exp.getId()));
        assertNull(expenseDAO.getExpenseById(exp.getId()));
    }

    @Test
    @Order(4)
    public void testTransactionsLedgerAndPayments() throws Exception {
        TransactionDAO txDAO = new TransactionDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        SupplierDAO supplierDAO = new SupplierDAO();

        Customer customer = new Customer("JUnit Khata Customer", "9988776655", "Kolhapur");
        customer.setOutstandingBalance(1000.0);
        assertTrue(customerDAO.addCustomer(customer));

        Supplier supplier = new Supplier("JUnit Khata Supplier", "9911223344", "Sangli");
        supplier.setOutstandingBalance(2000.0);
        assertTrue(supplierDAO.addSupplier(supplier));

        // Record customer payment (receipt)
        assertTrue(txDAO.recordCustomerPayment(customer.getId(), 400.0, "CASH", "Partial payment"));
        Customer afterPayment = customerDAO.getCustomerById(customer.getId());
        assertEquals(600.0, afterPayment.getOutstandingBalance(), 0.001);

        // Record supplier payment (outflow)
        assertTrue(txDAO.recordSupplierPayment(supplier.getId(), 500.0, "UPI", "Advance settlement"));
        Supplier afterSuppPayment = supplierDAO.getSupplierById(supplier.getId());
        assertEquals(1500.0, afterSuppPayment.getOutstandingBalance(), 0.001);

        // Cleanup
        customerDAO.deleteCustomer(customer.getId());
        supplierDAO.deleteSupplier(supplier.getId());
    }

    @Test
    @Order(5)
    public void testDashboardMetrics() {
        DashboardDAO dashboardDAO = new DashboardDAO();
        DashboardDAO.DashboardMetrics metrics = dashboardDAO.fetchMetrics();
        assertNotNull(metrics);
        assertTrue(metrics.productCount >= 0);
        assertTrue(metrics.inventoryValuation >= 0.0);
        assertNotNull(metrics.lowStockProducts);
    }

    @Test
    @Order(6)
    public void testMarketPricesOffline() {
        MarketPriceDAO dao = new MarketPriceDAO();
        List<MarketPrice> list = dao.getAllPrices();
        assertFalse(list.isEmpty(), "Cached offline APMC prices should not be empty");
        List<String> markets = dao.getDistinctMarkets();
        assertFalse(markets.isEmpty(), "APMC markets list should not be empty");
    }

    @Test
    @Order(7)
    public void testReportsAndCsvExport() throws Exception {
        ReportDAO reportDAO = new ReportDAO();
        ReportDAO.ReportResult stockReport = reportDAO.getInventoryReport();
        assertNotNull(stockReport);
        assertNotNull(stockReport.headers);
        assertFalse(stockReport.rows.isEmpty());

        // CSV Export test
        File temp = File.createTempFile("junit_test_export_", ".csv");
        temp.deleteOnExit();

        String[] headers = {"ID", "शेतमाल (Commodity)", "दर (Rate)"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"1", "कापूस (Cotton)", "7200.00"});

        CsvExporter.exportToFile(temp, headers, rows);

        byte[] content = Files.readAllBytes(temp.toPath());
        assertTrue(content.length >= 3);
        assertEquals((byte) 0xEF, content[0]);
        assertEquals((byte) 0xBB, content[1]);
        assertEquals((byte) 0xBF, content[2]); // UTF-8 BOM

        String text = new String(content, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(text.contains("कापूस"));
        temp.delete();
    }

    @Test
    @Order(8)
    public void testLocalizationKeys() {
        I18n.setLanguage("en");
        assertEquals("Purchases & Inward", I18n.get("purchase.title"));
        assertEquals("Operating Expenses", I18n.get("expense.title"));

        I18n.setLanguage("mr");
        assertFalse(I18n.get("purchase.title").contains("???"));
        assertFalse(I18n.get("expense.title").contains("???"));

        I18n.setLanguage("hi");
        assertFalse(I18n.get("purchase.title").contains("???"));
        assertFalse(I18n.get("expense.title").contains("???"));

        I18n.setLanguage("en");
    }

    private int getLatestProductId() throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM products ORDER BY id DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        throw new IllegalStateException("No product found");
    }
}
