package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.exception.InsufficientStockException;
import com.krishidb.model.Sale;
import com.krishidb.model.SaleItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    private final SyncQueueDAO syncQueueDAO = new SyncQueueDAO();

    // ---------------- CREATE SALE (ATOMIC TRANSACTION) ----------------
    public Sale createSale(Sale sale, List<SaleItem> items) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot complete sale: Cart is empty.");
        }

        // Validate payment method
        String paymentMethod = sale.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
            sale.setPaymentMethod(paymentMethod);
        } else {
            paymentMethod = paymentMethod.trim().toUpperCase();
            sale.setPaymentMethod(paymentMethod);
        }

        // Validate Credit / Khata requires registered customer
        boolean isCredit = "CREDIT".equalsIgnoreCase(paymentMethod) || "KHATA".equalsIgnoreCase(paymentMethod);
        if (isCredit && (sale.getCustomerId() == null || sale.getCustomerId() <= 0)) {
            throw new IllegalArgumentException("Khata / Credit payment requires a registered customer.");
        }

        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // Step 1: Validate customer exists if supplied
            if (sale.getCustomerId() != null && sale.getCustomerId() > 0) {
                String checkCustSql = "SELECT id, name FROM customers WHERE id = ?";
                try (PreparedStatement cs = connection.prepareStatement(checkCustSql)) {
                    cs.setInt(1, sale.getCustomerId());
                    try (ResultSet rs = cs.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Customer not found with ID: " + sale.getCustomerId());
                        }
                        if (sale.getCustomerName() == null || sale.getCustomerName().isEmpty()) {
                            sale.setCustomerName(rs.getString("name"));
                        }
                    }
                }
            }

            // Step 2: Validate product existence and stock availability
            double totalCalculated = 0.0;
            String checkProductSql = "SELECT id, name, unit, selling_price, stock_quantity FROM products WHERE id = ?";

            for (SaleItem item : items) {
                if (item.getQuantity() <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than zero for product ID: " + item.getProductId());
                }

                try (PreparedStatement psProd = connection.prepareStatement(checkProductSql)) {
                    psProd.setInt(1, item.getProductId());
                    try (ResultSet rs = psProd.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Product not found with ID: " + item.getProductId());
                        }
                        String prodName = rs.getString("name");
                        String unit = rs.getString("unit");
                        double currentStock = rs.getDouble("stock_quantity");

                        if (currentStock < item.getQuantity()) {
                            throw new InsufficientStockException(prodName, item.getQuantity(), currentStock);
                        }

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
                totalCalculated += subtotal;
            }

            totalCalculated = Math.round(totalCalculated * 100.0) / 100.0;
            sale.setTotalAmount(totalCalculated);

            // Step 3: Insert into sales table
            String insertSaleSql = """
                INSERT INTO sales (customer_id, total_amount, payment_method, notes, sync_status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """;
            int generatedSaleId;
            try (PreparedStatement psSale = connection.prepareStatement(insertSaleSql, Statement.RETURN_GENERATED_KEYS)) {
                if (sale.getCustomerId() != null && sale.getCustomerId() > 0) {
                    psSale.setInt(1, sale.getCustomerId());
                } else {
                    psSale.setNull(1, Types.INTEGER);
                }
                psSale.setDouble(2, sale.getTotalAmount());
                psSale.setString(3, sale.getPaymentMethod());
                psSale.setString(4, sale.getNotes());

                int rows = psSale.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Failed to insert sales record.");
                }

                try (ResultSet keys = psSale.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedSaleId = keys.getInt(1);
                        sale.setId(generatedSaleId);
                    } else {
                        throw new SQLException("Failed to retrieve generated sale ID.");
                    }
                }
            }

            // Step 4: Insert sale_items and guarded inventory deduction
            String insertItemSql = """
                INSERT INTO sale_items (sale_id, product_id, quantity, price_per_unit, subtotal)
                VALUES (?, ?, ?, ?, ?)
                """;

            String deductStockSql = """
                UPDATE products
                SET stock_quantity = stock_quantity - ?,
                    updated_at = CURRENT_TIMESTAMP,
                    sync_status = 'PENDING'
                WHERE id = ? AND stock_quantity >= ?
                """;

            for (SaleItem item : items) {
                item.setSaleId(generatedSaleId);
                try (PreparedStatement psItem = connection.prepareStatement(insertItemSql, Statement.RETURN_GENERATED_KEYS)) {
                    psItem.setInt(1, generatedSaleId);
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

                // Guarded inventory update
                try (PreparedStatement psDeduct = connection.prepareStatement(deductStockSql)) {
                    psDeduct.setDouble(1, item.getQuantity());
                    psDeduct.setInt(2, item.getProductId());
                    psDeduct.setDouble(3, item.getQuantity());

                    int affected = psDeduct.executeUpdate();
                    if (affected == 0) {
                        // Guard failed: concurrent sale or stock dropped below requested
                        throw new InsufficientStockException(
                                item.getProductName() != null ? item.getProductName() : ("Product #" + item.getProductId()),
                                item.getQuantity(),
                                0.0
                        );
                    }
                }
            }

            // Step 5: Insert transaction ledger entry
            String insertTxSql = """
                INSERT INTO transactions (transaction_type, reference_id, amount, payment_method, sync_status)
                VALUES ('SALE', ?, ?, ?, 'PENDING')
                """;
            try (PreparedStatement psTx = connection.prepareStatement(insertTxSql)) {
                psTx.setInt(1, generatedSaleId);
                psTx.setDouble(2, sale.getTotalAmount());
                psTx.setString(3, sale.getPaymentMethod());
                psTx.executeUpdate();
            }

            // Step 6: Queue synchronization record in sync_queue using the same connection
            syncQueueDAO.addToQueue(connection, "sales", generatedSaleId, "INSERT");

            // Step 7: Atomically commit the transaction
            connection.commit();

            sale.setItems(items);
            return sale;

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

    // ---------------- GET SALE BY ID ----------------
    public Sale getSaleById(int id) {
        String sql = """
            SELECT s.id, s.customer_id, c.name AS customer_name, s.total_amount,
                   s.payment_method, s.sale_date, s.notes, s.sync_status
            FROM sales s
            LEFT JOIN customers c ON s.customer_id = c.id
            WHERE s.id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sale.setItems(getSaleItems(id));
                    return sale;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching sale by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ---------------- GET SALE ITEMS FOR A SALE ----------------
    public List<SaleItem> getSaleItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = """
            SELECT si.id, si.sale_id, si.product_id, p.name AS product_name, p.unit,
                   si.quantity, si.price_per_unit, si.subtotal
            FROM sale_items si
            LEFT JOIN products p ON si.product_id = p.id
            WHERE si.sale_id = ?
            ORDER BY si.id ASC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, saleId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem(
                            rs.getInt("id"),
                            rs.getInt("sale_id"),
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
            System.err.println("Error fetching sale items for sale " + saleId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    // ---------------- GET ALL SALES ----------------
    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = """
            SELECT s.id, s.customer_id, c.name AS customer_name, s.total_amount,
                   s.payment_method, s.sale_date, s.notes, s.sync_status
            FROM sales s
            LEFT JOIN customers c ON s.customer_id = c.id
            ORDER BY s.id DESC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                sales.add(mapResultSetToSale(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all sales: " + e.getMessage());
            e.printStackTrace();
        }
        return sales;
    }

    // ---------------- SEARCH SALES ----------------
    public List<Sale> searchSales(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllSales();
        }

        List<Sale> results = new ArrayList<>();
        String pattern = "%" + query.trim() + "%";
        String sql = """
            SELECT s.id, s.customer_id, c.name AS customer_name, s.total_amount,
                   s.payment_method, s.sale_date, s.notes, s.sync_status
            FROM sales s
            LEFT JOIN customers c ON s.customer_id = c.id
            WHERE CAST(s.id AS TEXT) LIKE ?
               OR c.name LIKE ?
               OR s.payment_method LIKE ?
               OR s.sale_date LIKE ?
               OR s.notes LIKE ?
            ORDER BY s.id DESC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);
            statement.setString(5, pattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToSale(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching sales: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    // ---------------- SAFE SALE CANCELLATION / VOID ----------------
    public boolean cancelSale(int saleId) throws Exception {
        Sale sale = getSaleById(saleId);
        if (sale == null) {
            return false;
        }

        List<SaleItem> items = getSaleItems(saleId);

        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Restore product inventory
            String restoreStockSql = """
                UPDATE products
                SET stock_quantity = stock_quantity + ?,
                    updated_at = CURRENT_TIMESTAMP,
                    sync_status = 'PENDING'
                WHERE id = ?
                """;
            try (PreparedStatement psRestore = connection.prepareStatement(restoreStockSql)) {
                for (SaleItem item : items) {
                    psRestore.setDouble(1, item.getQuantity());
                    psRestore.setInt(2, item.getProductId());
                    psRestore.executeUpdate();
                }
            }

            // 2. Delete transactions entry
            String deleteTxSql = "DELETE FROM transactions WHERE transaction_type = 'SALE' AND reference_id = ?";
            try (PreparedStatement psTx = connection.prepareStatement(deleteTxSql)) {
                psTx.setInt(1, saleId);
                psTx.executeUpdate();
            }

            // 3. Delete sale items
            String deleteItemsSql = "DELETE FROM sale_items WHERE sale_id = ?";
            try (PreparedStatement psItems = connection.prepareStatement(deleteItemsSql)) {
                psItems.setInt(1, saleId);
                psItems.executeUpdate();
            }

            // 4. Delete sale
            String deleteSaleSql = "DELETE FROM sales WHERE id = ?";
            try (PreparedStatement psSale = connection.prepareStatement(deleteSaleSql)) {
                psSale.setInt(1, saleId);
                int rows = psSale.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Failed to delete sale ID " + saleId);
                }
            }

            // 5. Enqueue DELETE in sync_queue
            syncQueueDAO.addToQueue(connection, "sales", saleId, "DELETE");

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

    // ---------------- METRICS & AGGREGATIONS ----------------
    public double getTodaySalesTotal() {
        String sql = """
            SELECT COALESCE(SUM(total_amount), 0.0)
            FROM sales
            WHERE date(sale_date, 'localtime') = date('now', 'localtime')
               OR date(sale_date) = date('now')
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating today's sales total: " + e.getMessage());
        }
        return 0.0;
    }

    public int getTotalSalesCount() {
        String sql = "SELECT COUNT(*) FROM sales";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting total sales: " + e.getMessage());
        }
        return 0;
    }

    public int getPendingSalesCount() {
        String sql = "SELECT COUNT(*) FROM sales WHERE sync_status = 'PENDING'";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting pending sales: " + e.getMessage());
        }
        return 0;
    }

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Integer custId = rs.getObject("customer_id") != null ? rs.getInt("customer_id") : null;
        return new Sale(
                rs.getInt("id"),
                custId,
                rs.getString("customer_name"),
                rs.getDouble("total_amount"),
                rs.getString("payment_method"),
                rs.getString("sale_date"),
                rs.getString("notes"),
                rs.getString("sync_status")
        );
    }
}
