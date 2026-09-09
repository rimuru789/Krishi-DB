package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.TransactionRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private final SyncQueueDAO syncQueueDAO = new SyncQueueDAO();

    // ---------------- RECORD CUSTOMER PAYMENT (KHATA CLEARANCE) ----------------
    public boolean recordCustomerPayment(int customerId, double amount, String paymentMethod, String notes) throws Exception {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Valid customer must be selected.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
        } else {
            paymentMethod = paymentMethod.trim().toUpperCase();
        }

        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Verify customer exists and get name
            String custName;
            String checkCust = "SELECT name, outstanding_balance FROM customers WHERE id = ?";
            try (PreparedStatement psCust = connection.prepareStatement(checkCust)) {
                psCust.setInt(1, customerId);
                try (ResultSet rs = psCust.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Customer not found with ID: " + customerId);
                    }
                    custName = rs.getString("name");
                }
            }

            // 2. Update customer outstanding balance
            String updateBal = """
                UPDATE customers
                SET outstanding_balance = MAX(0.0, outstanding_balance - ?),
                    updated_at = CURRENT_TIMESTAMP,
                    sync_status = 'PENDING'
                WHERE id = ?
                """;
            try (PreparedStatement psBal = connection.prepareStatement(updateBal)) {
                psBal.setDouble(1, amount);
                psBal.setInt(2, customerId);
                psBal.executeUpdate();
            }

            // 3. Insert transaction record
            String insertTx = """
                INSERT INTO transactions (transaction_type, reference_id, amount, payment_method, description, sync_status)
                VALUES ('CUSTOMER_PAYMENT', ?, ?, ?, ?, 'PENDING')
                """;
            int generatedTxId;
            try (PreparedStatement psTx = connection.prepareStatement(insertTx, Statement.RETURN_GENERATED_KEYS)) {
                psTx.setInt(1, customerId);
                psTx.setDouble(2, amount);
                psTx.setString(3, paymentMethod);
                String desc = "Khata Payment received from " + custName +
                        (notes != null && !notes.trim().isEmpty() ? " - " + notes.trim() : "");
                psTx.setString(4, desc);
                psTx.executeUpdate();

                try (ResultSet keys = psTx.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedTxId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated transaction ID.");
                    }
                }
            }

            // 4. Queue in sync_queue
            syncQueueDAO.addToQueue(connection, "transactions", generatedTxId, "INSERT");

            // 5. Commit
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

    // ---------------- RECORD SUPPLIER PAYMENT ----------------
    public boolean recordSupplierPayment(int supplierId, double amount, String paymentMethod, String notes) throws Exception {
        if (supplierId <= 0) {
            throw new IllegalArgumentException("Valid supplier must be selected.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
        } else {
            paymentMethod = paymentMethod.trim().toUpperCase();
        }

        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Verify supplier exists and get name
            String supName;
            String checkSup = "SELECT name, outstanding_balance FROM suppliers WHERE id = ?";
            try (PreparedStatement psSup = connection.prepareStatement(checkSup)) {
                psSup.setInt(1, supplierId);
                try (ResultSet rs = psSup.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Supplier not found with ID: " + supplierId);
                    }
                    supName = rs.getString("name");
                }
            }

            // 2. Update supplier payable balance
            String updateBal = """
                UPDATE suppliers
                SET outstanding_balance = MAX(0.0, outstanding_balance - ?),
                    updated_at = CURRENT_TIMESTAMP,
                    sync_status = 'PENDING'
                WHERE id = ?
                """;
            try (PreparedStatement psBal = connection.prepareStatement(updateBal)) {
                psBal.setDouble(1, amount);
                psBal.setInt(2, supplierId);
                psBal.executeUpdate();
            }

            // 3. Insert transaction record
            String insertTx = """
                INSERT INTO transactions (transaction_type, reference_id, amount, payment_method, description, sync_status)
                VALUES ('SUPPLIER_PAYMENT', ?, ?, ?, ?, 'PENDING')
                """;
            int generatedTxId;
            try (PreparedStatement psTx = connection.prepareStatement(insertTx, Statement.RETURN_GENERATED_KEYS)) {
                psTx.setInt(1, supplierId);
                psTx.setDouble(2, amount);
                psTx.setString(3, paymentMethod);
                String desc = "Payment made to supplier " + supName +
                        (notes != null && !notes.trim().isEmpty() ? " - " + notes.trim() : "");
                psTx.setString(4, desc);
                psTx.executeUpdate();

                try (ResultSet keys = psTx.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedTxId = keys.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated transaction ID.");
                    }
                }
            }

            // 4. Queue in sync_queue
            syncQueueDAO.addToQueue(connection, "transactions", generatedTxId, "INSERT");

            // 5. Commit
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

    // ---------------- GET ALL TRANSACTIONS ----------------
    public List<TransactionRecord> getAllTransactions() {
        List<TransactionRecord> list = new ArrayList<>();
        String sql = """
            SELECT id, transaction_type, reference_id, amount, payment_method, description, transaction_date, sync_status
            FROM transactions
            ORDER BY id DESC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting transactions: " + e.getMessage());
        }
        return list;
    }

    // ---------------- FILTER TRANSACTIONS ----------------
    public List<TransactionRecord> filterTransactions(String typeFilter, String paymentFilter, String query) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, transaction_type, reference_id, amount, payment_method, description, transaction_date, sync_status
            FROM transactions
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (typeFilter != null && !typeFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(typeFilter)) {
            sql.append(" AND transaction_type = ?");
            params.add(typeFilter.trim());
        }

        if (paymentFilter != null && !paymentFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(paymentFilter)) {
            sql.append(" AND payment_method = ?");
            params.add(paymentFilter.trim());
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (CAST(id AS TEXT) LIKE ? OR description LIKE ? OR transaction_type LIKE ? OR transaction_date LIKE ?)");
            String pattern = "%" + query.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY id DESC");

        List<TransactionRecord> list = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering transactions: " + e.getMessage());
        }
        return list;
    }

    // ---------------- RECENT TRANSACTIONS (FOR DASHBOARD) ----------------
    public List<TransactionRecord> getRecentTransactions(int limit) {
        List<TransactionRecord> list = new ArrayList<>();
        String sql = """
            SELECT id, transaction_type, reference_id, amount, payment_method, description, transaction_date, sync_status
            FROM transactions
            ORDER BY id DESC
            LIMIT ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent transactions: " + e.getMessage());
        }
        return list;
    }

    // ---------------- FINANCIAL AGGREGATIONS ----------------
    public double getTotalInflow() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0.0)
            FROM transactions
            WHERE transaction_type IN ('SALE', 'CUSTOMER_PAYMENT')
            """;
        return fetchAggregateSum(sql);
    }

    public double getTotalOutflow() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0.0)
            FROM transactions
            WHERE transaction_type IN ('PURCHASE', 'EXPENSE', 'SUPPLIER_PAYMENT')
            """;
        return fetchAggregateSum(sql);
    }

    public double getNetCashFlow() {
        return Math.round((getTotalInflow() - getTotalOutflow()) * 100.0) / 100.0;
    }

    public double getTotalSales() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE transaction_type = 'SALE'";
        return fetchAggregateSum(sql);
    }

    public double getTotalPurchases() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE transaction_type = 'PURCHASE'";
        return fetchAggregateSum(sql);
    }

    public double getTotalExpenses() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE transaction_type = 'EXPENSE'";
        return fetchAggregateSum(sql);
    }

    public int getTotalCount() {
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting transactions: " + e.getMessage());
        }
        return 0;
    }

    private double fetchAggregateSum(String sql) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return Math.round(rs.getDouble(1) * 100.0) / 100.0;
            }
        } catch (SQLException e) {
            System.err.println("Error querying aggregate sum: " + e.getMessage());
        }
        return 0.0;
    }

    private TransactionRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        Integer refId = rs.getObject("reference_id") != null ? rs.getInt("reference_id") : null;
        String desc = null;
        try {
            desc = rs.getString("description");
        } catch (SQLException ignored) {}

        return new TransactionRecord(
                rs.getInt("id"),
                rs.getString("transaction_type"),
                refId,
                rs.getDouble("amount"),
                rs.getString("payment_method"),
                desc,
                rs.getString("transaction_date"),
                rs.getString("sync_status")
        );
    }
}
