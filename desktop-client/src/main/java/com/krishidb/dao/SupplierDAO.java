package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    // ---------------- ADD SUPPLIER ----------------
    public boolean addSupplier(Supplier supplier) {
        String sql = """
            INSERT INTO suppliers (name, phone, village, sync_status)
            VALUES (?, ?, ?, 'PENDING')
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getPhone());
            statement.setString(3, supplier.getVillage());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int supplierId = generatedKeys.getInt(1);
                        supplier.setId(supplierId);

                        SyncQueueDAO queueDAO = new SyncQueueDAO();
                        queueDAO.addToQueue("suppliers", supplierId, "INSERT");
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Failed to add supplier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- GET ALL SUPPLIERS ----------------
    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = """
            SELECT id, name, phone, village, created_at, updated_at, sync_status
            FROM suppliers
            ORDER BY name COLLATE NOCASE ASC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve suppliers: " + e.getMessage());
        }

        return suppliers;
    }

    // ---------------- GET SUPPLIER BY ID ----------------
    public Supplier getSupplierById(int id) {
        String sql = """
            SELECT id, name, phone, village, created_at, updated_at, sync_status
            FROM suppliers
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplier(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get supplier by id " + id + ": " + e.getMessage());
        }

        return null;
    }

    // ---------------- UPDATE SUPPLIER ----------------
    public boolean updateSupplier(Supplier supplier) {
        String sql = """
            UPDATE suppliers
            SET name = ?, phone = ?, village = ?, updated_at = CURRENT_TIMESTAMP, sync_status = 'PENDING'
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, supplier.getName());
            statement.setString(2, supplier.getPhone());
            statement.setString(3, supplier.getVillage());
            statement.setInt(4, supplier.getId());

            int rows = statement.executeUpdate();
            if (rows > 0) {
                SyncQueueDAO queueDAO = new SyncQueueDAO();
                queueDAO.addToQueue("suppliers", supplier.getId(), "UPDATE");
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Failed to update supplier: " + e.getMessage());
            return false;
        }
    }

    // ---------------- CHECK LINKED PURCHASES (FOREIGN KEY INTEGRITY) ----------------
    public int getLinkedPurchasesCount(int supplierId) {
        String sql = "SELECT COUNT(*) FROM purchases WHERE supplier_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, supplierId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking linked purchases for supplier " + supplierId + ": " + e.getMessage());
        }
        return 0;
    }

    // ---------------- DELETE SUPPLIER ----------------
    public boolean deleteSupplier(int id) {
        // Enforce referential safety before deleting
        if (getLinkedPurchasesCount(id) > 0) {
            return false;
        }

        String sql = "DELETE FROM suppliers WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Failed to delete supplier: " + e.getMessage());
            return false;
        }
    }

    // ---------------- SEARCH SUPPLIERS ----------------
    public List<Supplier> searchSuppliers(String query) {
        List<Supplier> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllSuppliers();
        }

        String pattern = "%" + query.trim() + "%";
        String sql = """
            SELECT id, name, phone, village, created_at, updated_at, sync_status
            FROM suppliers
            WHERE name LIKE ? OR phone LIKE ? OR village LIKE ?
            ORDER BY name COLLATE NOCASE ASC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Search suppliers failed: " + e.getMessage());
        }

        return results;
    }

    // ---------------- METRICS & AGGREGATIONS ----------------
    public int getPendingCount() {
        String sql = "SELECT COUNT(*) FROM suppliers WHERE sync_status = 'PENDING'";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting pending suppliers: " + e.getMessage());
        }
        return 0;
    }

    public int getDistinctVillageCount() {
        String sql = "SELECT COUNT(DISTINCT village) FROM suppliers WHERE village IS NOT NULL AND TRIM(village) != ''";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting supplier locations: " + e.getMessage());
        }
        return 0;
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        return new Supplier(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("village"),
                rs.getString("created_at"),
                rs.getString("updated_at"),
                rs.getString("sync_status")
        );
    }
}
