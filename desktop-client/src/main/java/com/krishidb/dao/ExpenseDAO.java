package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    private final SyncQueueDAO syncQueueDAO = new SyncQueueDAO();

    // ---------------- ADD EXPENSE (ATOMIC TRANSACTION) ----------------
    public boolean addExpense(Expense expense) throws Exception {
        if (expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense category is required.");
        }

        String paymentMethod = expense.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            paymentMethod = "CASH";
            expense.setPaymentMethod(paymentMethod);
        } else {
            paymentMethod = paymentMethod.trim().toUpperCase();
            expense.setPaymentMethod(paymentMethod);
        }

        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Insert into expenses table
            String insertExpenseSql = """
                INSERT INTO expenses (category, description, amount, payment_method, sync_status)
                VALUES (?, ?, ?, ?, 'PENDING')
                """;

            int generatedExpenseId;
            try (PreparedStatement ps = connection.prepareStatement(insertExpenseSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, expense.getCategory().trim());
                ps.setString(2, expense.getDescription() != null ? expense.getDescription().trim() : "");
                ps.setDouble(3, expense.getAmount());
                ps.setString(4, expense.getPaymentMethod());

                int rows = ps.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Failed to insert expense record.");
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        generatedExpenseId = keys.getInt(1);
                        expense.setId(generatedExpenseId);
                    } else {
                        throw new SQLException("Failed to retrieve generated expense ID.");
                    }
                }
            }

            // 2. Insert into transactions table (Ledger)
            String insertTxSql = """
                INSERT INTO transactions (transaction_type, reference_id, amount, payment_method, description, sync_status)
                VALUES ('EXPENSE', ?, ?, ?, ?, 'PENDING')
                """;
            try (PreparedStatement psTx = connection.prepareStatement(insertTxSql)) {
                psTx.setInt(1, generatedExpenseId);
                psTx.setDouble(2, expense.getAmount());
                psTx.setString(3, expense.getPaymentMethod());
                String desc = "Expense [" + expense.getCategory() + "]: " +
                        (expense.getDescription() != null && !expense.getDescription().isEmpty()
                                ? expense.getDescription()
                                : ("Expense #" + generatedExpenseId));
                psTx.setString(4, desc);
                psTx.executeUpdate();
            }

            // 3. Queue in sync_queue
            syncQueueDAO.addToQueue(connection, "expenses", generatedExpenseId, "INSERT");

            // 4. Commit atomically
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

    // ---------------- UPDATE EXPENSE ----------------
    public boolean updateExpense(Expense expense) throws Exception {
        if (expense.getAmount() <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero.");
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Expense category is required.");
        }

        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            String updateExpenseSql = """
                UPDATE expenses
                SET category = ?, description = ?, amount = ?, payment_method = ?, sync_status = 'PENDING'
                WHERE id = ?
                """;

            try (PreparedStatement ps = connection.prepareStatement(updateExpenseSql)) {
                ps.setString(1, expense.getCategory().trim());
                ps.setString(2, expense.getDescription() != null ? expense.getDescription().trim() : "");
                ps.setDouble(3, expense.getAmount());
                ps.setString(4, expense.getPaymentMethod());
                ps.setInt(5, expense.getId());

                int rows = ps.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Expense not found with ID: " + expense.getId());
                }
            }

            // Update corresponding ledger entry
            String updateTxSql = """
                UPDATE transactions
                SET amount = ?, payment_method = ?, description = ?, sync_status = 'PENDING'
                WHERE transaction_type = 'EXPENSE' AND reference_id = ?
                """;
            try (PreparedStatement psTx = connection.prepareStatement(updateTxSql)) {
                psTx.setDouble(1, expense.getAmount());
                psTx.setString(2, expense.getPaymentMethod());
                String desc = "Expense [" + expense.getCategory() + "]: " +
                        (expense.getDescription() != null && !expense.getDescription().isEmpty()
                                ? expense.getDescription()
                                : ("Expense #" + expense.getId()));
                psTx.setString(3, desc);
                psTx.setInt(4, expense.getId());
                psTx.executeUpdate();
            }

            // Queue UPDATE in sync_queue
            syncQueueDAO.addToQueue(connection, "expenses", expense.getId(), "UPDATE");

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

    // ---------------- DELETE EXPENSE ----------------
    public boolean deleteExpense(int id) throws Exception {
        Connection connection = DatabaseManager.getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            connection.setAutoCommit(false);

            // 1. Delete transactions entry
            String deleteTxSql = "DELETE FROM transactions WHERE transaction_type = 'EXPENSE' AND reference_id = ?";
            try (PreparedStatement psTx = connection.prepareStatement(deleteTxSql)) {
                psTx.setInt(1, id);
                psTx.executeUpdate();
            }

            // 2. Delete expense entry
            String deleteExpenseSql = "DELETE FROM expenses WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteExpenseSql)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                if (rows <= 0) {
                    throw new SQLException("Expense not found with ID: " + id);
                }
            }

            // 3. Queue DELETE in sync_queue
            syncQueueDAO.addToQueue(connection, "expenses", id, "DELETE");

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

    // ---------------- GET EXPENSE BY ID ----------------
    public Expense getExpenseById(int id) {
        String sql = """
            SELECT id, category, description, amount, payment_method, expense_date, sync_status
            FROM expenses
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExpense(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting expense by ID: " + e.getMessage());
        }
        return null;
    }

    // ---------------- GET ALL EXPENSES ----------------
    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        String sql = """
            SELECT id, category, description, amount, payment_method, expense_date, sync_status
            FROM expenses
            ORDER BY id DESC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all expenses: " + e.getMessage());
        }
        return expenses;
    }

    // ---------------- SEARCH & FILTER EXPENSES ----------------
    public List<Expense> searchExpenses(String query, String categoryFilter) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, category, description, amount, payment_method, expense_date, sync_status
            FROM expenses
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (categoryFilter != null && !categoryFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(categoryFilter)) {
            sql.append(" AND category = ?");
            params.add(categoryFilter.trim());
        }

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (CAST(id AS TEXT) LIKE ? OR category LIKE ? OR description LIKE ? OR payment_method LIKE ? OR expense_date LIKE ?)");
            String pattern = "%" + query.trim() + "%";
            for (int i = 0; i < 5; i++) {
                params.add(pattern);
            }
        }

        sql.append(" ORDER BY id DESC");

        List<Expense> results = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToExpense(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching expenses: " + e.getMessage());
        }
        return results;
    }

    // ---------------- METRICS & AGGREGATIONS ----------------
    public double getTotalExpensesAmount() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return Math.round(rs.getDouble(1) * 100.0) / 100.0;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total expenses: " + e.getMessage());
        }
        return 0.0;
    }

    public double getTodayExpensesAmount() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0.0)
            FROM expenses
            WHERE date(expense_date, 'localtime') = date('now', 'localtime')
               OR date(expense_date) = date('now')
            """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return Math.round(rs.getDouble(1) * 100.0) / 100.0;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating today's expenses: " + e.getMessage());
        }
        return 0.0;
    }

    public int getTotalExpensesCount() {
        String sql = "SELECT COUNT(*) FROM expenses";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting expenses: " + e.getMessage());
        }
        return 0;
    }

    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        String payMethod = "CASH";
        try {
            payMethod = rs.getString("payment_method");
            if (payMethod == null) payMethod = "CASH";
        } catch (SQLException ignored) {}

        return new Expense(
                rs.getInt("id"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("amount"),
                payMethod,
                rs.getString("expense_date"),
                rs.getString("sync_status")
        );
    }
}
