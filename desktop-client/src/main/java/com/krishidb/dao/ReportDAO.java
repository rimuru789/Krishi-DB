package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public static class ReportResult {
        public String[] headers;
        public List<Object[]> rows = new ArrayList<>();
        public int recordCount = 0;
        public double totalValue = 0.0;
    }

    // ---------------- SALES REPORT ----------------
    public ReportResult getSalesReport(String period, String customFrom, String customTo) {
        ReportResult res = new ReportResult();
        res.headers = new String[]{"Sale ID", "Date", "Customer", "Payment Method", "Notes", "Total (\u20B9)"};

        StringBuilder sql = new StringBuilder("""
            SELECT s.id, s.sale_date, COALESCE(c.name, 'Walk-in Customer'), s.payment_method, COALESCE(s.notes, ''), s.total_amount
            FROM sales s
            LEFT JOIN customers c ON s.customer_id = c.id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, "s.sale_date", period, customFrom, customTo);
        sql.append(" ORDER BY s.id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String date = rs.getString(2);
                    String customer = rs.getString(3);
                    String payment = rs.getString(4);
                    String notes = rs.getString(5);
                    double amount = rs.getDouble(6);

                    res.rows.add(new Object[]{id, date, customer, payment, notes, String.format("%.2f", amount)});
                    res.recordCount++;
                    res.totalValue += amount;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating sales report: " + e.getMessage());
        }

        res.totalValue = Math.round(res.totalValue * 100.0) / 100.0;
        return res;
    }

    // ---------------- PURCHASE REPORT ----------------
    public ReportResult getPurchaseReport(String period, String customFrom, String customTo) {
        ReportResult res = new ReportResult();
        res.headers = new String[]{"Purchase ID", "Date", "Supplier", "Invoice #", "Payment Method", "Total (\u20B9)"};

        StringBuilder sql = new StringBuilder("""
            SELECT p.id, p.purchase_date, COALESCE(s.name, '-'), COALESCE(p.invoice_number, '-'), p.payment_method, p.total_amount
            FROM purchases p
            LEFT JOIN suppliers s ON p.supplier_id = s.id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, "p.purchase_date", period, customFrom, customTo);
        sql.append(" ORDER BY p.id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String date = rs.getString(2);
                    String supplier = rs.getString(3);
                    String invoice = rs.getString(4);
                    String payment = rs.getString(5);
                    double amount = rs.getDouble(6);

                    res.rows.add(new Object[]{id, date, supplier, invoice, payment, String.format("%.2f", amount)});
                    res.recordCount++;
                    res.totalValue += amount;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating purchase report: " + e.getMessage());
        }

        res.totalValue = Math.round(res.totalValue * 100.0) / 100.0;
        return res;
    }

    // ---------------- EXPENSE REPORT ----------------
    public ReportResult getExpenseReport(String period, String customFrom, String customTo) {
        ReportResult res = new ReportResult();
        res.headers = new String[]{"Expense ID", "Date", "Category", "Description", "Payment Method", "Amount (\u20B9)"};

        StringBuilder sql = new StringBuilder("""
            SELECT id, expense_date, category, description, payment_method, amount
            FROM expenses
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, "expense_date", period, customFrom, customTo);
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String date = rs.getString(2);
                    String category = rs.getString(3);
                    String desc = rs.getString(4);
                    String payment = rs.getString(5);
                    double amount = rs.getDouble(6);

                    res.rows.add(new Object[]{id, date, category, desc, payment, String.format("%.2f", amount)});
                    res.recordCount++;
                    res.totalValue += amount;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating expense report: " + e.getMessage());
        }

        res.totalValue = Math.round(res.totalValue * 100.0) / 100.0;
        return res;
    }

    // ---------------- INVENTORY / STOCK REPORT ----------------
    public ReportResult getInventoryReport() {
        ReportResult res = new ReportResult();
        res.headers = new String[]{"Product ID", "Product Name", "Category", "Stock", "Unit", "Selling Price (\u20B9)", "Valuation (\u20B9)", "Stock Status"};

        String sql = """
            SELECT id, name, category, stock_quantity, unit, selling_price,
                   (stock_quantity * selling_price) AS valuation,
                   CASE WHEN stock_quantity <= low_stock_level THEN 'LOW STOCK' ELSE 'NORMAL' END AS status
            FROM products
            ORDER BY name COLLATE NOCASE ASC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String category = rs.getString(3);
                double stock = rs.getDouble(4);
                String unit = rs.getString(5);
                double price = rs.getDouble(6);
                double val = rs.getDouble(7);
                String status = rs.getString(8);

                res.rows.add(new Object[]{
                        id, name, category,
                        String.format("%.2f", stock),
                        unit,
                        String.format("%.2f", price),
                        String.format("%.2f", val),
                        status
                });
                res.recordCount++;
                res.totalValue += val;
            }
        } catch (SQLException e) {
            System.err.println("Error generating inventory report: " + e.getMessage());
        }

        res.totalValue = Math.round(res.totalValue * 100.0) / 100.0;
        return res;
    }

    // ---------------- TRANSACTION / FINANCIAL LEDGER REPORT ----------------
    public ReportResult getTransactionReport(String period, String customFrom, String customTo) {
        ReportResult res = new ReportResult();
        res.headers = new String[]{"Transaction ID", "Date", "Type", "Ref #", "Description", "Payment Mode", "Amount (\u20B9)"};

        StringBuilder sql = new StringBuilder("""
            SELECT id, transaction_date, transaction_type, reference_id, description, payment_method, amount
            FROM transactions
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();
        appendDateFilter(sql, params, "transaction_date", period, customFrom, customTo);
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String date = rs.getString(2);
                    String type = rs.getString(3);
                    Integer refId = rs.getObject(4) != null ? rs.getInt(4) : null;
                    String desc = rs.getString(5);
                    String payment = rs.getString(6);
                    double amount = rs.getDouble(7);

                    boolean isInflow = "SALE".equalsIgnoreCase(type) || "CUSTOMER_PAYMENT".equalsIgnoreCase(type);
                    String prefix = isInflow ? "+" : "-";

                    res.rows.add(new Object[]{
                            id, date, type, (refId != null ? refId : "-"),
                            (desc != null ? desc : ""), payment,
                            prefix + " " + String.format("%.2f", amount)
                    });
                    res.recordCount++;
                    if (isInflow) {
                        res.totalValue += amount;
                    } else {
                        res.totalValue -= amount;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating transaction report: " + e.getMessage());
        }

        res.totalValue = Math.round(res.totalValue * 100.0) / 100.0;
        return res;
    }

    // ---------------- DATE FILTER HELPER ----------------
    private void appendDateFilter(StringBuilder sql, List<Object> params, String col,
                                  String period, String customFrom, String customTo) {
        if (period == null || "ALL_TIME".equalsIgnoreCase(period)) {
            return;
        }

        if ("TODAY".equalsIgnoreCase(period)) {
            sql.append(" AND (date(").append(col).append(", 'localtime') = date('now', 'localtime') OR date(").append(col).append(") = date('now'))");
        } else if ("YESTERDAY".equalsIgnoreCase(period)) {
            sql.append(" AND date(").append(col).append(", 'localtime') = date('now', 'localtime', '-1 day')");
        } else if ("THIS_WEEK".equalsIgnoreCase(period)) {
            sql.append(" AND date(").append(col).append(", 'localtime') >= date('now', 'localtime', 'weekday 0', '-6 days')");
        } else if ("THIS_MONTH".equalsIgnoreCase(period)) {
            sql.append(" AND date(").append(col).append(", 'localtime') >= date('now', 'localtime', 'start of month')");
        } else if ("CUSTOM".equalsIgnoreCase(period)) {
            if (customFrom != null && !customFrom.trim().isEmpty()) {
                sql.append(" AND date(").append(col).append(") >= ?");
                params.add(customFrom.trim());
            }
            if (customTo != null && !customTo.trim().isEmpty()) {
                sql.append(" AND date(").append(col).append(") <= ?");
                params.add(customTo.trim());
            }
        }
    }

    private void bindParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }
}
