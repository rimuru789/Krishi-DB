package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Purchase;
import com.krishidb.model.PurchaseItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    private final SyncQueueDAO syncQueueDAO = new SyncQueueDAO();

    // ---------------- CREATE PURCHASE (ATOMIC TRANSACTION) ----------------
    public Purchase createPurchase(Purchase purchase, List<PurchaseItem> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot complete purchase: Item list is empty.");
        }

        // Normalize payment method
        String paymentMethod = purchase.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
            purchase.setPaymentMethod(paymentMethod);
        } else {
            paymentMethod = paymentMethod.trim().toUpperCase();
            purchase.setPaymentMethod(paymentMethod);
        }

        boolean isCredit = "CREDIT".equalsIgnoreCase(paymentMethod) || "KHATA".equalsIgnoreCase(paymentMethod);
        if (isCredit && (purchase.getSupplierId() == null || purchase.getSupplierId() <= 0)) {
            throw new IllegalArgumentException("Credit purchase requires selecting a registered supplier.");
        }

        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // Step 1: Validate supplier exists if supplied
            if (purchase.getSupplierId() != null && purchase.getSupplierId() > 0) {
                String checkSupSql = "SELECT id, name FROM suppliers WHERE id = ?";
                try (PreparedStatement psSup = connection.prepareStatement(checkSupSql)) {
                    psSup.setInt(1, purchase.getSupplierId());
                    try (ResultSet rs = psSup.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Supplier not found with ID: " + purchase.getSupplierId());
                        }
                        if (purchase.getSupplierName() == null || purchase.getSupplierName().isEmpty()) {
                            purchase.setSupplierName(rs.getString("name"));
                        }
                    }
                }
            }

            // Step 2: Validate products exist, quantity > 0, price >= 0, calculate subtotals & total
            double calculatedTotal = 0.0;
            String checkProductSql = "SELECT id, name, unit, stock_quantity FROM products WHERE id = ?";

            for (PurchaseItem item : items) {
                if (item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than zero for product ID: " + item.getProductId());
                }
                if (item.getPricePerUnit() < 0) {
                    throw new IllegalArgumentException("Purchase price cannot be negative for product ID: " + item.getProductId());
                }

                try (PreparedStatement psProd = connection.prepareStatement(checkProductSql)) {
                    psProd.setInt(1, item.getProductId());
                    try (ResultSet rs = psProd.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Product not found with ID: " + item.getProductId());
                        }
                        String prodName = rs.getString("name");
                        String unit = rs.getString("unit");

                        if (item.getProductName() == null || item.getProductName().isEmpty()) {
                            item.setProductName(prodName);
                        }
                        if (item.getUnit() == null || item.getUnit().isEmpty()) {
                            item.setUnit(unit);
                        }
                    }
                }

                double subtotal = Math.round(item.getQuantity() * item.getPricePerUnit() * 100.0) / 100.0;
                item.setSubtotal(subtotal);
                calculatedTotal += subtotal;
            }

            calculatedTotal = Math.round(calculatedTotal * 100.0) / 100.0;
            purchase.setTotalAmount(calculatedTotal);

            // Step 3: Insert purchase header
            String insertPurchaseSql = """
                INSERT INTO purchases (supplier_id, invoice_number, total_amount, payment_method, notes, sync_status)
                VALUES (?, ?, ?, ?, ?, 'PENDING')
                """;
            int generatedPurchaseId;
            try (PreparedStatement psPurchase = connection.prepareStatement(insertPurchaseSql, Statement.RETURN_GENERATED_KEYS)) {
                if (purchase.getSupplierId() != null && purchase.getSupplierId() > 0) {
                    psPurchase.setInt(1, purchase.getSupplierId());
                } else {
                    psPurchase.setNull(1, Types.INTEGER);
                }
                psPurchase.setString(2, purchase.getInvoiceNumber());
                psPurchase.setDouble(3, purchase.getTotalAmount());
                psPurchase.setString(4, purchase.getPaymentMethod());
                psPurchase.setString(5, purchase.getNotes());

                int rows = psPurchase.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Failed to insert purchase record.");
                }

                try (ResultSet keys = psPurchase.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedPurchaseId = keys.getInt(1);
                        purchase.setId(generatedPurchaseId);
                    } else {
                        throw new SQLException("Failed to retrieve generated purchase ID.");
                    }
                }
            }

            // Step 4: Insert purchase_items and increase product stock
            String insertItemSql = """
                INSERT INTO purchase_items (purchase_id, product_id, quantity, price_per_unit, subtotal)
                VALUES (?, ?, ?, ?, ?)
                """;

            String increaseStockSql = """
                UPDATE products
                SET stock_quantity = stock_quantity + ?,
                    updated_at = CURRENT_TIMESTAMP,
                    sync_status = 'PENDING'
                WHERE id = ?
                """;

            for (PurchaseItem item : items) {
                item.setPurchaseId(generatedPurchaseId);
                try (PreparedStatement psItem = connection.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS)) {
                    psItem.setInt(1, generatedPurchaseId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setDouble(3, item.getQuantity());
                    psItem.setDouble(4, item.getPricePerUnit());
                    psItem.setDouble(5, item.getSubtotal());
                    psItem.executeUpdate();

                    try (ResultSet itemKeys = psItem.getGeneratedKeys()) {
                        if (itemKeys.next()) {
                            item.setId(itemKeys.getInt(1));
                        }
                    }
                }

                // Increase inventory stock
                try (PreparedStatement psStock = connection.prepareStatement(increaseStockSql)) {
                    psStock.setDouble(1, item.getQuantity());
                    psStock.setInt(2, item.getProductId());
                    psStock.executeUpdate();
                }
            }

            // Step 5: If Credit purchase, increase supplier payable balance
            if (isCredit && purchase.getSupplierId() != null && purchase.getSupplierId() > 0) {
                String updateSupBalSql = """
                    UPDATE suppliers
                    SET outstanding_balance = COALESCE(outstanding_balance, 0.0) + ?,
                        updated_at = CURRENT_TIMESTAMP,
                        sync_status = 'PENDING'
                    WHERE id = ?
                    """;
                try (PreparedStatement psBal = connection.prepareStatement(updateSupBalSql)) {
                    psBal.setDouble(1, purchase.getTotalAmount());
                    psBal.setInt(2, purchase.getSupplierId());
                    psBal.executeUpdate();
                }
            }

            // Step 6: Insert transaction ledger record
            String insertTxSql = """
                INSERT INTO transactions (transaction_type, reference_id, amount, payment_method, description, sync_status)
                VALUES ('PURCHASE', ?, ?, ?, ?, 'PENDING')
                """;
            try (PreparedStatement psTx = connection.prepareStatement(insertTxSql)) {
                psTx.setInt(1, generatedPurchaseId);
                psTx.setDouble(2, purchase.getTotalAmount());
                psTx.setString(3, purchase.getPaymentMethod());
                String desc = "Purchase Inward #" + generatedPurchaseId +
                        (purchase.getSupplierName() != null ? " (" + purchase.getSupplierName() + ")" : "");
                psTx.setString(4, desc);
                psTx.executeUpdate();
            }

            // Step 7: Queue synchronization event in sync_queue
            syncQueueDAO.addToQueue(connection, "purchases", generatedPurchaseId, "INSERT");

            // Step 8: Commit atomically
            connection.commit();

            purchase.setItems(items);
            return purchase;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
                connection.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    // ---------------- CANCEL / VOID PURCHASE ----------------
    public boolean cancelPurchase(int purchaseId) throws Exception {
        Purchase purchase = getPurchaseById(purchaseId);
        if (purchase == null) {
            return false;
        }

        List<PurchaseItem> items = getPurchaseItems(purchaseId);

        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Guard check: Check product stocks to ensure deducting will NOT make stock negative
            String checkStockSql = "SELECT stock_quantity, name FROM products WHERE id = ?";
            for (PurchaseItem item : items) {
                try (PreparedStatement psCheck = connection.prepareStatement(checkStockSql)) {
                    psCheck.setInt(1, item.getProductId());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            double currentStock = rs.getDouble("stock_quantity");
                            if (currentStock < item.getQuantity()) {
                                throw new IllegalStateException("Cannot cancel purchase: Stock for '" +
                                        rs.getString("name") + "' would become negative (" +
                                        (currentStock - item.getQuantity()) + ").");
                            }
                        }
                    }
                }
            }

            // 2. Subtract previously added quantities from stock
            String deductStockSql = """
                UPDATE products
                SET stock_quantity = stock_quantity - ?,
                    updated_at = CURRENT_TIMESTAMP,
                    sync_status = 'PENDING'
                WHERE id = ?
                """;
            try (PreparedStatement psDeduct = connection.prepareStatement(deductStockSql)) {
                for (PurchaseItem item : items) {
                    psDeduct.setDouble(1, item.getQuantity());
                    psDeduct.setInt(2, item.getProductId());
                    psDeduct.executeUpdate();
                }
            }

            // 3. Revert supplier balance if credit purchase
            boolean isCredit = "CREDIT".equalsIgnoreCase(purchase.getPaymentMethod()) ||
                               "KHATA".equalsIgnoreCase(purchase.getPaymentMethod());
            if (isCredit && purchase.getSupplierId() != null && purchase.getSupplierId() > 0) {
                String revertSupBalSql = """
                    UPDATE suppliers
                    SET outstanding_balance = MAX(0.0, COALESCE(outstanding_balance, 0.0) - ?),
                        updated_at = CURRENT_TIMESTAMP,
                        sync_status = 'PENDING'
                    WHERE id = ?
                    """;
                try (PreparedStatement psBal = connection.prepareStatement(revertSupBalSql)) {
                    psBal.setDouble(1, purchase.getTotalAmount());
                    psBal.setInt(2, purchase.getSupplierId());
                    psBal.executeUpdate();
                }
            }

            // 4. Delete related ledger entry
            String deleteTxSql = "DELETE FROM transactions WHERE transaction_type = 'PURCHASE' AND reference_id = ?";
            try (PreparedStatement psTx = connection.prepareStatement(deleteTxSql)) {
                psTx.setInt(1, purchaseId);
                psTx.executeUpdate();
            }

            // 5. Delete purchase items
            String deleteItemsSql = "DELETE FROM purchase_items WHERE purchase_id = ?";
            try (PreparedStatement psItems = connection.prepareStatement(deleteItemsSql)) {
                psItems.setInt(1, purchaseId);
                psItems.executeUpdate();
            }

            // 6. Delete purchase
            String deletePurchaseSql = "DELETE FROM purchases WHERE id = ?";
            try (PreparedStatement psPurchase = connection.prepareStatement(deletePurchaseSql)) {
                psPurchase.setInt(1, purchaseId);
                int rows = psPurchase.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Failed to delete purchase ID " + purchaseId);
                }
            }

            // 7. Enqueue DELETE in sync_queue
            syncQueueDAO.addToQueue(connection, "purchases", purchaseId, "DELETE");

            connection.commit();
            return true;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
                connection.close();
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    // ---------------- GET PURCHASE BY ID ----------------
    public Purchase getPurchaseById(int id) {
        String sql = """
            SELECT p.id, p.supplier_id, s.name AS supplier_name, p.invoice_number,
                   p.total_amount, p.payment_method, p.purchase_date, p.notes, p.sync_status
            FROM purchases p
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            WHERE p.id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Purchase purchase = mapResultSetToPurchase(rs);
                    purchase.setItems(getPurchaseItems(id));
                    return purchase;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching purchase by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ---------------- GET PURCHASE ITEMS FOR A PURCHASE ----------------
    public List<PurchaseItem> getPurchaseItems(int purchaseId) {
        List<PurchaseItem> items = new ArrayList<>();
        String sql = """
            SELECT pi.id, pi.purchase_id, pi.product_id, pr.name AS product_name, pr.unit,
                   pi.quantity, pi.price_per_unit, pi.subtotal
            FROM purchase_items pi
            LEFT JOIN products pr ON pi.product_id = pr.id
            WHERE pi.purchase_id = ?
            ORDER BY pi.id ASC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, purchaseId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PurchaseItem item = new PurchaseItem(
                            rs.getInt("id"),
                            rs.getInt("purchase_id"),
                            rs.getInt("product_id"),
                            rs.getString("product_name"),
                            rs.getString("unit"),
                            rs.getDouble("quantity"),
                            rs.getDouble("price_per_unit"),
                            rs.getDouble("subtotal")
                    );
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching purchase items for purchase " + purchaseId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    // ---------------- GET ALL PURCHASES ----------------
    public List<Purchase> getAllPurchases() {
        List<Purchase> purchases = new ArrayList<>();
        String sql = """
            SELECT p.id, p.supplier_id, s.name AS supplier_name, p.invoice_number,
                   p.total_amount, p.payment_method, p.purchase_date, p.notes, p.sync_status
            FROM purchases p
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            ORDER BY p.id DESC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                purchases.add(mapResultSetToPurchase(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all purchases: " + e.getMessage());
            e.printStackTrace();
        }
        return purchases;
    }

    // ---------------- SEARCH PURCHASES ----------------
    public List<Purchase> searchPurchases(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPurchases();
        }

        List<Purchase> results = new ArrayList<>();
        String pattern = "%" + query.trim() + "%";
        String sql = """
            SELECT p.id, p.supplier_id, s.name AS supplier_name, p.invoice_number,
                   p.total_amount, p.payment_method, p.purchase_date, p.notes, p.sync_status
            FROM purchases p
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            WHERE CAST(p.id AS TEXT) LIKE ?
               OR s.name LIKE ?
               OR p.invoice_number LIKE ?
               OR p.payment_method LIKE ?
               OR p.purchase_date LIKE ?
               OR p.notes LIKE ?
            ORDER BY p.id DESC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);
            statement.setString(6, pattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToPurchase(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching purchases: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    // ---------------- METRICS & AGGREGATIONS ----------------
    public double getTodayPurchasesTotal() {
        String sql = """
            SELECT COALESCE(SUM(total_amount), 0.0)
            FROM purchases
            WHERE date(purchase_date, 'localtime') = date('now', 'localtime')
               OR date(purchase_date) = date('now')
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return Math.round(rs.getDouble(1) * 100.0) / 100.0;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating today's purchases total: " + e.getMessage());
        }
        return 0.0;
    }

    public int getTotalPurchasesCount() {
        String sql = "SELECT COUNT(*) FROM purchases";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total purchases: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalPurchasesAmount() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0.0) FROM purchases";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return Math.round(rs.getDouble(1) * 100.0) / 100.0;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total purchases amount: " + e.getMessage());
        }
        return 0.0;
    }

    private Purchase mapResultSetToPurchase(ResultSet rs) throws SQLException {
        Integer supId = rs.getObject("supplier_id") != null ? rs.getInt("supplier_id") : null;
        String invNum = null;
        try {
            invNum = rs.getString("invoice_number");
        } catch (SQLException ignored) {}

        String payMethod = "CASH";
        try {
            payMethod = rs.getString("payment_method");
            if (payMethod == null) payMethod = "CASH";
        } catch (SQLException ignored) {}

        return new Purchase(
                rs.getInt("id"),
                supId,
                rs.getString("supplier_name"),
                invNum,
                rs.getDouble("total_amount"),
                payMethod,
                rs.getString("purchase_date"),
                rs.getString("notes"),
                rs.getString("sync_status")
        );
    }
}
