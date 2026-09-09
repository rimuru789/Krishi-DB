package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Product;
import com.krishidb.model.TransactionRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    public static class DashboardMetrics {
        public double todaySales;
        public int todayInvoices;
        public double todayPurchases;
        public int todayPurchasesCount;
        public double todayExpenses;
        public int todayExpensesCount;
        public double totalSales;
        public double totalPurchases;
        public double totalExpenses;
        public double netProfit;
        public double inventoryValuation;
        public int productCount;
        public int inStockCount;
        public int lowStockCount;
        public double customerOutstanding;
        public double supplierOutstanding;
        public int pendingSyncCount;
        public List<TransactionRecord> recentTransactions = new ArrayList<>();
        public List<Product> lowStockProducts = new ArrayList<>();
    }

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ProductDAO productDAO = new ProductDAO();

    public DashboardMetrics fetchMetrics() {
        DashboardMetrics m = new DashboardMetrics();

        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Today's sales & invoices
            String todaySalesSql = """
                SELECT COALESCE(SUM(total_amount), 0.0), COUNT(*)
                FROM sales
                WHERE date(sale_date, 'localtime') = date('now', 'localtime')
                   OR date(sale_date) = date('now')
                """;
            try (PreparedStatement ps = conn.prepareStatement(todaySalesSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.todaySales = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                    m.todayInvoices = rs.getInt(2);
                }
            }

            // 1b. Today's purchases & count
            String todayPurchasesSql = """
                SELECT COALESCE(SUM(total_amount), 0.0), COUNT(*)
                FROM purchases
                WHERE date(purchase_date, 'localtime') = date('now', 'localtime')
                   OR date(purchase_date) = date('now')
                """;
            try (PreparedStatement ps = conn.prepareStatement(todayPurchasesSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.todayPurchases = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                    m.todayPurchasesCount = rs.getInt(2);
                }
            }

            // 1c. Today's expenses & count
            String todayExpensesSql = """
                SELECT COALESCE(SUM(amount), 0.0), COUNT(*)
                FROM expenses
                WHERE date(expense_date, 'localtime') = date('now', 'localtime')
                   OR date(expense_date) = date('now')
                """;
            try (PreparedStatement ps = conn.prepareStatement(todayExpensesSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.todayExpenses = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                    m.todayExpensesCount = rs.getInt(2);
                }
            }

            // 2. Total sales
            String totalSalesSql = "SELECT COALESCE(SUM(total_amount), 0.0) FROM sales";
            try (PreparedStatement ps = conn.prepareStatement(totalSalesSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.totalSales = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                }
            }

            // 3. Total purchases
            String totalPurchasesSql = "SELECT COALESCE(SUM(total_amount), 0.0) FROM purchases";
            try (PreparedStatement ps = conn.prepareStatement(totalPurchasesSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.totalPurchases = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                }
            }

            // 4. Total expenses
            String totalExpensesSql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses";
            try (PreparedStatement ps = conn.prepareStatement(totalExpensesSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.totalExpenses = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                }
            }

            // 5. Net profit / balance
            m.netProfit = Math.round((m.totalSales - m.totalPurchases - m.totalExpenses) * 100.0) / 100.0;

            // 6. Inventory valuation & product counts
            String invSql = """
                SELECT COALESCE(SUM(stock_quantity * selling_price), 0.0),
                       COUNT(*),
                       COALESCE(SUM(CASE WHEN stock_quantity <= low_stock_level THEN 1 ELSE 0 END), 0),
                       COALESCE(SUM(CASE WHEN stock_quantity > 0 THEN 1 ELSE 0 END), 0)
                FROM products
                """;
            try (PreparedStatement ps = conn.prepareStatement(invSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.inventoryValuation = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                    m.productCount = rs.getInt(2);
                    m.lowStockCount = rs.getInt(3);
                    m.inStockCount = rs.getInt(4);
                }
            }

            // 7. Customer & Supplier balances
            String custBalSql = "SELECT COALESCE(SUM(outstanding_balance), 0.0) FROM customers";
            try (PreparedStatement ps = conn.prepareStatement(custBalSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.customerOutstanding = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                }
            }

            String supBalSql = "SELECT COALESCE(SUM(outstanding_balance), 0.0) FROM suppliers";
            try (PreparedStatement ps = conn.prepareStatement(supBalSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.supplierOutstanding = Math.round(rs.getDouble(1) * 100.0) / 100.0;
                }
            }

            // 8. Pending sync items count across all tables
            String syncCountSql = """
                SELECT (SELECT COUNT(*) FROM products WHERE sync_status = 'PENDING') +
                       (SELECT COUNT(*) FROM customers WHERE sync_status = 'PENDING') +
                       (SELECT COUNT(*) FROM suppliers WHERE sync_status = 'PENDING') +
                       (SELECT COUNT(*) FROM sales WHERE sync_status = 'PENDING') +
                       (SELECT COUNT(*) FROM purchases WHERE sync_status = 'PENDING') +
                       (SELECT COUNT(*) FROM expenses WHERE sync_status = 'PENDING')
                """;
            try (PreparedStatement ps = conn.prepareStatement(syncCountSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.pendingSyncCount = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching dashboard metrics: " + e.getMessage());
            e.printStackTrace();
        }

        // Recent transactions & low stock products
        m.recentTransactions = transactionDAO.getRecentTransactions(8);
        m.lowStockProducts = productDAO.getLowStockProducts();

        return m;
    }
}
