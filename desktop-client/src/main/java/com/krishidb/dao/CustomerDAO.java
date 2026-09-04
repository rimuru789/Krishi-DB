package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // ---------------- ADD CUSTOMER ----------------
    public boolean addCustomer(Customer customer) {
        String sql = """
            INSERT INTO customers (name, phone, village, sync_status)
            VALUES (?, ?, ?, 'PENDING')
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, customer.getName());
            statement.setString(2, customer.getPhone());
            statement.setString(3, customer.getVillage());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int customerId = generatedKeys.getInt(1);
                        customer.setId(customerId);

                        SyncQueueDAO queueDAO = new SyncQueueDAO();
                        queueDAO.addToQueue("customers", customerId, "INSERT");
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Failed to add customer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- GET ALL CUSTOMERS ----------------
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = """
            SELECT id, name, phone, village, created_at, updated_at, sync_status
            FROM customers
            ORDER BY name COLLATE NOCASE ASC
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            System.err.println("Failed to retrieve customers: " + e.getMessage());
        }

        return customers;
    }

    // ---------------- GET CUSTOMER BY ID ----------------
    public Customer getCustomerById(int id) {
        String sql = """
            SELECT id, name, phone, village, created_at, updated_at, sync_status
            FROM customers
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get customer by id " + id + ": " + e.getMessage());
        }

        return null;
    }

    // ---------------- UPDATE CUSTOMER ----------------
    public boolean updateCustomer(Customer customer) {
        String sql = """
            UPDATE customers
            SET name = ?, phone = ?, village = ?, updated_at = CURRENT_TIMESTAMP, sync_status = 'PENDING'
            WHERE id = ?
            """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getName());
            statement.setString(2, customer.getPhone());
            statement.setString(3, customer.getVillage());
            statement.setInt(4, customer.getId());

            int rows = statement.executeUpdate();
            if (rows > 0) {
                SyncQueueDAO queueDAO = new SyncQueueDAO();
                queueDAO.addToQueue("customers", customer.getId(), "UPDATE");
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("Failed to update customer: " + e.getMessage());
            return false;
        }
    }

    // ---------------- CHECK LINKED SALES (FOREIGN KEY INTEGRITY) ----------------
    public int getLinkedSalesCount(int customerId) {
        String sql = "SELECT COUNT(*) FROM sales WHERE customer_id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking linked sales for customer " + customerId + ": " + e.getMessage());
        }
        return 0;
    }

    // ---------------- DELETE CUSTOMER ----------------
    public boolean deleteCustomer(int id) {
        // Enforce referential safety before deleting
        if (getLinkedSalesCount(id) > 0) {
            return false;
        }

        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Failed to delete customer: " + e.getMessage());
            return false;
        }
    }

    // ---------------- SEARCH CUSTOMERS ----------------
    public List<Customer> searchCustomers(String query) {
        List<Customer> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllCustomers();
        }

        String pattern = "%" + query.trim() + "%";
        String sql = """
            SELECT id, name, phone, village, created_at, updated_at, sync_status
            FROM customers
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
                    results.add(mapResultSetToCustomer(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Search customers failed: " + e.getMessage());
        }

        return results;
    }

    // ---------------- METRICS & AGGREGATIONS ----------------
    public int getPendingCount() {
        String sql = "SELECT COUNT(*) FROM customers WHERE sync_status = 'PENDING'";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting pending customers: " + e.getMessage());
        }
        return 0;
    }

    public int getDistinctVillageCount() {
        String sql = "SELECT COUNT(DISTINCT village) FROM customers WHERE village IS NOT NULL AND TRIM(village) != ''";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting customer villages: " + e.getMessage());
        }
        return 0;
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        return new Customer(
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
