package com.krishidb;

import com.krishidb.dao.*;
import com.krishidb.database.DatabaseInitializer;
import com.krishidb.database.DatabaseManager;
import com.krishidb.exception.InsufficientStockException;
import com.krishidb.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BusinessLogicBugFixTest {

    @TempDir
    static Path tempDir;

    private static CustomerDAO customerDAO;
    private static SupplierDAO supplierDAO;
    private static ProductDAO productDAO;
    private static SaleDAO saleDAO;
    private static PurchaseDAO purchaseDAO;
    private static ExpenseDAO expenseDAO;
    private static TransactionDAO transactionDAO;

    @BeforeAll
    public static void setUpAll() {
        Path isolatedDb = tempDir.resolve("isolated_bugfix_test.db");
        DatabaseManager.setCustomDatabasePath(isolatedDb);
        DatabaseInitializer.initialize();

        customerDAO = new CustomerDAO();
        supplierDAO = new SupplierDAO();
        productDAO = new ProductDAO();
        saleDAO = new SaleDAO();
        purchaseDAO = new PurchaseDAO();
        expenseDAO = new ExpenseDAO();
        transactionDAO = new TransactionDAO();
    }

    @AfterAll
    public static void tearDownAll() {
        DatabaseManager.resetDatabasePath();
    }

    // =========================================================================
    // P1 — PRODUCT GENERATED ID
    // =========================================================================

    @Test
    @Order(10)
    public void testProductGeneratedIdAssignedBackToObject() throws Exception {
        Product product = new Product("Fertilizer Urea 46%", "Fertilizer", "bag", 266.50, 100.0, 10.0);
        assertEquals(0, product.getId(), "Pre-condition: Product ID should initially be 0");

        boolean added = productDAO.addProduct(product);
        assertTrue(added, "addProduct() should succeed");
        assertTrue(product.getId() > 0, "Product ID must be greater than zero after insertion");

        Product retrieved = productDAO.getProductById(product.getId());
        assertNotNull(retrieved, "Product must exist in SQLite DB by generated ID");
        assertEquals(product.getId(), retrieved.getId(), "Retrieved product ID must match assigned ID");
        assertEquals("Fertilizer Urea 46%", retrieved.getName());
    }

    // =========================================================================
    // P0 — CREDIT / KHATA SALES: CREATE SALE
    // =========================================================================

    @Test
    @Order(20)
    public void testCreditSaleIncreasesCustomerBalance() throws Exception {
        Customer customer = new Customer("Ramesh Patil", "9876543210", "Vadgaon");
        assertTrue(customerDAO.addCustomer(customer));
        int customerId = customer.getId();
        assertEquals(0.0, customer.getOutstandingBalance(), 0.001);

        Product product = new Product("DAP Fertilizer", "Fertilizer", "bag", 1350.0, 50.0, 5.0);
        assertTrue(productDAO.addProduct(product));
        int productId = product.getId();

        Sale sale = new Sale();
        sale.setCustomerId(customerId);
        sale.setPaymentMethod("CREDIT");
        sale.setNotes("Khata purchase of 2 bags DAP");

        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(productId, product.getName(), product.getUnit(), 2.0, 1350.0));

        Sale completedSale = saleDAO.createSale(sale, items);
        assertNotNull(completedSale);
        assertEquals(2700.0, completedSale.getTotalAmount(), 0.001);

        // Verify customer outstanding balance increased by sale amount
        Customer updatedCustomer = customerDAO.getCustomerById(customerId);
        assertNotNull(updatedCustomer);
        assertEquals(2700.0, updatedCustomer.getOutstandingBalance(), 0.001,
                "Customer outstanding balance must increase by exact credit sale amount");

        // Verify stock deducted
        Product updatedProduct = productDAO.getProductById(productId);
        assertEquals(48.0, updatedProduct.getStockQuantity(), 0.001);
    }

    @Test
    @Order(21)
    public void testKhataSynonymSaleIncreasesCustomerBalance() throws Exception {
        Customer customer = new Customer("Suresh Jadhav", "9876543211", "Kavathe");
        assertTrue(customerDAO.addCustomer(customer));
        int customerId = customer.getId();

        Product product = new Product("Potash", "Fertilizer", "bag", 900.0, 20.0, 5.0);
        assertTrue(productDAO.addProduct(product));

        Sale sale = new Sale();
        sale.setCustomerId(customerId);
        sale.setPaymentMethod("KHATA");

        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(product.getId(), product.getName(), product.getUnit(), 1.0, 900.0));

        Sale completedSale = saleDAO.createSale(sale, items);
        assertNotNull(completedSale);

        Customer updatedCustomer = customerDAO.getCustomerById(customerId);
        assertEquals(900.0, updatedCustomer.getOutstandingBalance(), 0.001,
                "KHATA payment method must also increase customer balance");
    }

    @Test
    @Order(22)
    public void testCashAndUpiSalesDoNotIncreaseCustomerBalance() throws Exception {
        Customer customer = new Customer("Anil Shinde", "9876543212", "Tasgaon");
        assertTrue(customerDAO.addCustomer(customer));
        int customerId = customer.getId();

        Product product = new Product("Pesticide Chlorpyrifos", "Pesticide", "litre", 450.0, 30.0, 5.0);
        assertTrue(productDAO.addProduct(product));

        // 1. CASH sale
        Sale cashSale = new Sale();
        cashSale.setCustomerId(customerId);
        cashSale.setPaymentMethod("CASH");

        List<SaleItem> cashItems = new ArrayList<>();
        cashItems.add(new SaleItem(product.getId(), product.getName(), product.getUnit(), 2.0, 450.0));
        saleDAO.createSale(cashSale, cashItems);

        Customer custAfterCash = customerDAO.getCustomerById(customerId);
        assertEquals(0.0, custAfterCash.getOutstandingBalance(), 0.001,
                "CASH sale must NOT increase customer balance");

        // 2. UPI sale
        Sale upiSale = new Sale();
        upiSale.setCustomerId(customerId);
        upiSale.setPaymentMethod("UPI");

        List<SaleItem> upiItems = new ArrayList<>();
        upiItems.add(new SaleItem(product.getId(), product.getName(), product.getUnit(), 1.0, 450.0));
        saleDAO.createSale(upiSale, upiItems);

        Customer custAfterUpi = customerDAO.getCustomerById(customerId);
        assertEquals(0.0, custAfterUpi.getOutstandingBalance(), 0.001,
                "UPI sale must NOT increase customer balance");
    }

    @Test
    @Order(23)
    public void testFailedCreditSaleRollsBackAllEffects() throws Exception {
        Customer customer = new Customer("Balasaheb Pawar", "9876543213", "Shirala");
        assertTrue(customerDAO.addCustomer(customer));
        int customerId = customer.getId();

        Product product = new Product("Fungicide Mancozeb", "Pesticide", "kg", 600.0, 5.0, 2.0);
        assertTrue(productDAO.addProduct(product));
        int productId = product.getId();

        // Attempt to buy 10 kg when only 5 kg available -> InsufficientStockException
        Sale creditSale = new Sale();
        creditSale.setCustomerId(customerId);
        creditSale.setPaymentMethod("CREDIT");

        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(productId, product.getName(), product.getUnit(), 10.0, 600.0));

        assertThrows(InsufficientStockException.class, () -> saleDAO.createSale(creditSale, items));

        // Verify customer balance was NOT updated (rolled back)
        Customer customerAfterFail = customerDAO.getCustomerById(customerId);
        assertEquals(0.0, customerAfterFail.getOutstandingBalance(), 0.001,
                "Failed credit sale must roll back customer balance changes");

        // Verify stock was NOT deducted
        Product productAfterFail = productDAO.getProductById(productId);
        assertEquals(5.0, productAfterFail.getStockQuantity(), 0.001,
                "Failed credit sale must roll back product stock changes");

        // Verify no orphaned sale record in sales table
        assertEquals(0, saleDAO.searchSales("Shirala").size());
    }

    @Test
    @Order(24)
    public void testCreditSaleRequiresRegisteredCustomer() throws Exception {
        Product product = new Product("Zinc Sulphate", "Micronutrients", "kg", 80.0, 50.0, 5.0);
        assertTrue(productDAO.addProduct(product));

        Sale creditSaleWithoutCustomer = new Sale();
        creditSaleWithoutCustomer.setCustomerId(null);
        creditSaleWithoutCustomer.setPaymentMethod("CREDIT");

        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(product.getId(), product.getName(), product.getUnit(), 1.0, 80.0));

        assertThrows(IllegalArgumentException.class, () -> saleDAO.createSale(creditSaleWithoutCustomer, items));

        // Non-existent customer ID
        Sale creditSaleInvalidCust = new Sale();
        creditSaleInvalidCust.setCustomerId(999999);
        creditSaleInvalidCust.setPaymentMethod("CREDIT");

        assertThrows(IllegalArgumentException.class, () -> saleDAO.createSale(creditSaleInvalidCust, items));
    }

    // =========================================================================
    // P0 — CREDIT / KHATA SALES: CANCEL SALE
    // =========================================================================

    @Test
    @Order(30)
    public void testCreditSaleCancellationReversesCustomerReceivable() throws Exception {
        Customer customer = new Customer("Santosh Deshmukh", "9876543214", "Walwa");
        assertTrue(customerDAO.addCustomer(customer));
        int customerId = customer.getId();

        Product product = new Product("Hybrid Tomato Seeds", "Seeds", "packet", 750.0, 20.0, 2.0);
        assertTrue(productDAO.addProduct(product));
        int productId = product.getId();

        Sale sale = new Sale();
        sale.setCustomerId(customerId);
        sale.setPaymentMethod("CREDIT");

        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(productId, product.getName(), product.getUnit(), 2.0, 750.0));

        Sale completed = saleDAO.createSale(sale, items);
        int saleId = completed.getId();

        assertEquals(1500.0, customerDAO.getCustomerById(customerId).getOutstandingBalance(), 0.001);
        assertEquals(18.0, productDAO.getProductById(productId).getStockQuantity(), 0.001);

        // Cancel sale
        boolean cancelled = saleDAO.cancelSale(saleId);
        assertTrue(cancelled, "cancelSale() should return true");

        // Verify customer receivable reversed to 0
        Customer afterCancel = customerDAO.getCustomerById(customerId);
        assertEquals(0.0, afterCancel.getOutstandingBalance(), 0.001,
                "Cancelled credit sale must reverse customer receivable effect");

        // Verify stock restored
        Product restoredProduct = productDAO.getProductById(productId);
        assertEquals(20.0, restoredProduct.getStockQuantity(), 0.001,
                "Product stock must be restored after cancellation");

        // Verify sale deleted
        assertNull(saleDAO.getSaleById(saleId));

        // Duplicate cancellation returns false and does not double-reverse
        assertFalse(saleDAO.cancelSale(saleId), "Duplicate cancellation must return false");
        assertEquals(0.0, customerDAO.getCustomerById(customerId).getOutstandingBalance(), 0.001);
    }

    @Test
    @Order(31)
    public void testPaymentsFollowedByCancellationDoNotCorruptBalances() throws Exception {
        Customer customer = new Customer("Vijay Mane", "9876543215", "Islampur");
        assertTrue(customerDAO.addCustomer(customer));
        int customerId = customer.getId();

        Product product = new Product("NPK 19:19:19", "Fertilizer", "kg", 180.0, 40.0, 5.0);
        assertTrue(productDAO.addProduct(product));
        int productId = product.getId();

        // 1. Credit sale of Rs 1800 (10 kg @ 180)
        Sale sale = new Sale();
        sale.setCustomerId(customerId);
        sale.setPaymentMethod("CREDIT");

        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(productId, product.getName(), product.getUnit(), 10.0, 180.0));
        Sale completed = saleDAO.createSale(sale, items);
        int saleId = completed.getId();

        assertEquals(1800.0, customerDAO.getCustomerById(customerId).getOutstandingBalance(), 0.001);

        // 2. Customer makes partial repayment of Rs 800
        assertTrue(transactionDAO.recordCustomerPayment(customerId, 800.0, "CASH", "Partial payment"));
        Customer afterPayment = customerDAO.getCustomerById(customerId);
        assertEquals(1000.0, afterPayment.getOutstandingBalance(), 0.001);

        // 3. Attempt to cancel credit sale of Rs 1800 when balance is only Rs 1000.
        // Full reversal of Rs 1800 would make balance -800, which corrupts accounting.
        // The safe restriction must block the cancellation!
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> saleDAO.cancelSale(saleId));
        assertTrue(ex.getMessage().contains("less than sale amount") || ex.getMessage().contains("Customer payments"),
                "Exception message must clearly report restriction: " + ex.getMessage());

        // Verify balance was NOT corrupted to negative or wiped to 0
        Customer afterBlockedCancel = customerDAO.getCustomerById(customerId);
        assertEquals(1000.0, afterBlockedCancel.getOutstandingBalance(), 0.001,
                "Customer balance must remain 1000.0 after blocked cancellation");

        // Verify sale still exists in DB
        assertNotNull(saleDAO.getSaleById(saleId));

        // Verify stock was not restored incorrectly
        assertEquals(30.0, productDAO.getProductById(productId).getStockQuantity(), 0.001);
    }

    // =========================================================================
    // P1 — FINANCIAL CASH-FLOW AGGREGATION
    // =========================================================================

    @Test
    @Order(40)
    public void testCreditSaleFollowedByRepaymentNotDoubleCountedInInflow() throws Exception {
        double initialInflow = transactionDAO.getTotalInflow();

        Customer customer = new Customer("Ganesh Kadam", "9876543216", "Miraj");
        assertTrue(customerDAO.addCustomer(customer));
        int customerId = customer.getId();

        Product product = new Product("Cotton Seeds BG II", "Seeds", "packet", 850.0, 50.0, 5.0);
        assertTrue(productDAO.addProduct(product));

        // Step 1: Make a CREDIT sale of Rs 1700 (2 packets @ 850)
        Sale creditSale = new Sale();
        creditSale.setCustomerId(customerId);
        creditSale.setPaymentMethod("CREDIT");

        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(product.getId(), product.getName(), product.getUnit(), 2.0, 850.0));
        saleDAO.createSale(creditSale, items);

        // At this point, the credit sale is a receivable, NOT cash received.
        // Total cash inflow MUST NOT increase!
        double inflowAfterCreditSale = transactionDAO.getTotalInflow();
        assertEquals(initialInflow, inflowAfterCreditSale, 0.001,
                "Credit sale must NOT be counted as cash inflow at sale time");

        // Step 2: Customer makes an actual repayment of Rs 1700 in CASH
        assertTrue(transactionDAO.recordCustomerPayment(customerId, 1700.0, "CASH", "Full clearance"));

        // Total cash inflow must now increase by exactly Rs 1700 (the actual payment received)
        double inflowAfterPayment = transactionDAO.getTotalInflow();
        assertEquals(initialInflow + 1700.0, inflowAfterPayment, 0.001,
                "Actual customer payment must be counted as cash inflow once, without double counting");
    }

    @Test
    @Order(41)
    public void testCreditPurchaseFollowedBySupplierRepaymentNotDoubleCountedInOutflow() throws Exception {
        double initialOutflow = transactionDAO.getTotalOutflow();

        Supplier supplier = new Supplier("Mahyco Seeds Ltd", "9822001122", "Jalna");
        assertTrue(supplierDAO.addSupplier(supplier));
        int supplierId = supplier.getId();

        Product product = new Product("Bajra Hybrid Seeds", "Seeds", "kg", 150.0, 0.0, 5.0);
        assertTrue(productDAO.addProduct(product));

        // Step 1: Make a CREDIT purchase of Rs 3000 (30 kg @ 100)
        Purchase creditPurchase = new Purchase();
        creditPurchase.setSupplierId(supplierId);
        creditPurchase.setInvoiceNumber("INV-CREDIT-P1");
        creditPurchase.setPaymentMethod("CREDIT");

        List<PurchaseItem> items = new ArrayList<>();
        items.add(new PurchaseItem(product.getId(), product.getName(), product.getUnit(), 30.0, 100.0));
        purchaseDAO.createPurchase(creditPurchase, items);

        // Credit purchase is a payable, NOT cash paid out.
        // Outflow MUST NOT increase!
        double outflowAfterCreditPurchase = transactionDAO.getTotalOutflow();
        assertEquals(initialOutflow, outflowAfterCreditPurchase, 0.001,
                "Credit purchase must NOT be counted as cash outflow at purchase time");

        // Step 2: Business pays the supplier Rs 3000 via UPI
        assertTrue(transactionDAO.recordSupplierPayment(supplierId, 3000.0, "UPI", "Bill settlement"));

        // Total cash outflow must now increase by exactly Rs 3000
        double outflowAfterPayment = transactionDAO.getTotalOutflow();
        assertEquals(initialOutflow + 3000.0, outflowAfterPayment, 0.001,
                "Supplier payment must be counted as cash outflow once, without double counting");
    }

    @Test
    @Order(42)
    public void testCashSaleAndExpenseCountedInCashFlow() throws Exception {
        double beforeInflow = transactionDAO.getTotalInflow();
        double beforeOutflow = transactionDAO.getTotalOutflow();

        Product product = new Product("Sulphur 80% WDG", "Fertilizer", "kg", 120.0, 50.0, 5.0);
        assertTrue(productDAO.addProduct(product));

        // Cash sale Rs 600
        Sale cashSale = new Sale();
        cashSale.setPaymentMethod("CASH");
        List<SaleItem> items = new ArrayList<>();
        items.add(new SaleItem(product.getId(), product.getName(), product.getUnit(), 5.0, 120.0));
        saleDAO.createSale(cashSale, items);

        assertEquals(beforeInflow + 600.0, transactionDAO.getTotalInflow(), 0.001,
                "Cash sale must count towards cash inflow");

        // Expense Rs 250
        Expense expense = new Expense("Transport", "Tempo rent for fertilizers", 250.0, "CASH");
        assertTrue(expenseDAO.addExpense(expense));

        assertEquals(beforeOutflow + 250.0, transactionDAO.getTotalOutflow(), 0.001,
                "Operating expense must count towards cash outflow");
    }

    // =========================================================================
    // P2 — CUSTOMER / SUPPLIER DELETION INTEGRITY
    // =========================================================================

    @Test
    @Order(50)
    public void testCustomerDeletionBlockedByPaymentHistory() throws Exception {
        Customer customer = new Customer("Pandurang More", "9876543217", "Ashta");
        assertTrue(customerDAO.addCustomer(customer));
        int customerId = customer.getId();

        // Customer has no sales, but has a payment transaction
        assertTrue(transactionDAO.recordCustomerPayment(customerId, 500.0, "CASH", "Opening advance"));
        assertEquals(1, customerDAO.getLinkedTransactionsCount(customerId));

        // Attempt deletion: Must be blocked!
        boolean deleted = customerDAO.deleteCustomer(customerId);
        assertFalse(deleted, "Customer deletion must be BLOCKED when payment transactions reference the customer");

        Customer stillExists = customerDAO.getCustomerById(customerId);
        assertNotNull(stillExists, "Customer must still exist in DB after blocked deletion");
    }

    @Test
    @Order(51)
    public void testSupplierDeletionBlockedByPaymentHistory() throws Exception {
        Supplier supplier = new Supplier("Godrej Agrovet Ltd", "9822334455", "Mumbai");
        assertTrue(supplierDAO.addSupplier(supplier));
        int supplierId = supplier.getId();

        // Supplier has no purchases, but has a payment transaction
        assertTrue(transactionDAO.recordSupplierPayment(supplierId, 1200.0, "BANK_TRANSFER", "Advance payment"));
        assertEquals(1, supplierDAO.getLinkedTransactionsCount(supplierId));

        // Attempt deletion: Must be blocked!
        boolean deleted = supplierDAO.deleteSupplier(supplierId);
        assertFalse(deleted, "Supplier deletion must be BLOCKED when payment transactions reference the supplier");

        Supplier stillExists = supplierDAO.getSupplierById(supplierId);
        assertNotNull(stillExists, "Supplier must still exist in DB after blocked deletion");
    }

    @Test
    @Order(52)
    public void testSafeCustomerAndSupplierDeletionWhenUnlinked() throws Exception {
        // Completely unlinked customer
        Customer customer = new Customer("Temporary Customer", "9900000001", "Solapur");
        assertTrue(customerDAO.addCustomer(customer));
        int custId = customer.getId();

        assertTrue(customerDAO.deleteCustomer(custId), "Unlinked customer must be deletable");
        assertNull(customerDAO.getCustomerById(custId));

        // Completely unlinked supplier
        Supplier supplier = new Supplier("Temporary Supplier", "9900000002", "Nashik");
        assertTrue(supplierDAO.addSupplier(supplier));
        int suppId = supplier.getId();

        assertTrue(supplierDAO.deleteSupplier(suppId), "Unlinked supplier must be deletable");
        assertNull(supplierDAO.getSupplierById(suppId));
    }
}
