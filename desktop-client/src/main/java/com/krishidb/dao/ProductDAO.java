package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

public class ProductDAO {

    // INSERT PRODUCT
    public boolean addProduct(Product product) {

        String sql = """
                INSERT INTO products
                (name, category, unit, selling_price,
                 stock_quantity, low_stock_level, sync_status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
        connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        );) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setString(3, product.getUnit());
            statement.setDouble(4, product.getSellingPrice());
            statement.setDouble(5, product.getStockQuantity());
            statement.setDouble(6, product.getLowStockLevel());
            statement.setString(7, product.getSyncStatus());

            int rowsAffected = statement.executeUpdate();


if(rowsAffected > 0){

    ResultSet generatedKeys = statement.getGeneratedKeys();

    if(generatedKeys.next()){

        int productId = generatedKeys.getInt(1);


        SyncQueueDAO queueDAO = new SyncQueueDAO();

        queueDAO.addToQueue(
                "products",
                productId,
                "INSERT"
        );

    }

    return true;
}


return false;

        } catch (SQLException e) {

            System.out.println("Failed to add product.");
            e.printStackTrace();

            return false;
        }
    }


    // READ ALL PRODUCTS
    public List<Product> getAllProducts() {

        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT id, name, category, unit,
                       selling_price, stock_quantity,
                       low_stock_level, sync_status
                FROM products
                ORDER BY name
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Product product = new Product(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("category"),
                        resultSet.getString("unit"),
                        resultSet.getDouble("selling_price"),
                        resultSet.getDouble("stock_quantity"),
                        resultSet.getDouble("low_stock_level"),
                        resultSet.getString("sync_status")
                );

                products.add(product);
            }

        } catch (SQLException e) {

            System.out.println("Failed to retrieve products.");
            e.printStackTrace();
        }

        return products;
    }
// UPDATE PRODUCT
public boolean updateProduct(Product product) {

    String sql = """
            UPDATE products
            SET name=?,
                category=?,
                unit=?,
                selling_price=?,
                stock_quantity=?,
                low_stock_level=?,
                sync_status='PENDING',
                updated_at=CURRENT_TIMESTAMP
            WHERE id=?
            """;

    try (Connection connection = DatabaseManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, product.getName());
        statement.setString(2, product.getCategory());
        statement.setString(3, product.getUnit());
        statement.setDouble(4, product.getSellingPrice());
        statement.setDouble(5, product.getStockQuantity());
        statement.setDouble(6, product.getLowStockLevel());
        statement.setInt(7, product.getId());

        int rows = statement.executeUpdate();
        if (rows > 0) {
            SyncQueueDAO queueDAO = new SyncQueueDAO();
            queueDAO.addToQueue(connection, "products", product.getId(), "UPDATE");
            return true;
        }
        return false;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

// DELETE PRODUCT
public boolean deleteProduct(int id) {
    // Foreign key check: protect ledger integrity
    String checkSales = "SELECT COUNT(*) FROM sale_items WHERE product_id = ?";
    String checkPurchases = "SELECT COUNT(*) FROM purchase_items WHERE product_id = ?";

    try (Connection connection = DatabaseManager.getConnection()) {
        try (PreparedStatement ps = connection.prepareStatement(checkSales)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("Cannot delete product: linked to " + rs.getInt(1) + " sale items.");
                    return false;
                }
            }
        }
        try (PreparedStatement ps = connection.prepareStatement(checkPurchases)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.err.println("Cannot delete product: linked to " + rs.getInt(1) + " purchase items.");
                    return false;
                }
            }
        }

        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                SyncQueueDAO queueDAO = new SyncQueueDAO();
                queueDAO.addToQueue(connection, "products", id, "DELETE");
                return true;
            }
        }
    } catch (SQLException e) {
        System.err.println("Failed to delete product: " + e.getMessage());
        e.printStackTrace();
    }
    return false;
}

// GET PRODUCT BY ID
public Product getProductById(int id) {
    String sql = """
            SELECT id, name, category, unit,
                   selling_price, stock_quantity,
                   low_stock_level, sync_status
            FROM products
            WHERE id = ?
            """;
    try (Connection connection = DatabaseManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setInt(1, id);
        try (ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("unit"),
                        rs.getDouble("selling_price"),
                        rs.getDouble("stock_quantity"),
                        rs.getDouble("low_stock_level"),
                        rs.getString("sync_status")
                );
            }
        }
    } catch (SQLException e) {
        System.err.println("Failed to get product by id: " + e.getMessage());
    }
    return null;
}

// COUNT PENDING SYNC PRODUCTS
public int getPendingCount() {
    String sql = "SELECT COUNT(*) FROM products WHERE sync_status='PENDING'";
    try (Connection connection = DatabaseManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
            return resultSet.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

// TOTAL INVENTORY VALUATION
public double getTotalInventoryValue() {
    String sql = "SELECT COALESCE(SUM(stock_quantity * selling_price), 0.0) FROM products";
    try (Connection connection = DatabaseManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
            return Math.round(rs.getDouble(1) * 100.0) / 100.0;
        }
    } catch (SQLException e) {
        System.err.println("Error calculating inventory value: " + e.getMessage());
    }
    return 0.0;
}

// COUNT LOW STOCK PRODUCTS
public int getLowStockCount() {
    String sql = "SELECT COUNT(*) FROM products WHERE stock_quantity <= low_stock_level";
    try (Connection connection = DatabaseManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet rs = statement.executeQuery()) {
        if (rs.next()) {
            return rs.getInt(1);
        }
    } catch (SQLException e) {
        System.err.println("Error counting low stock products: " + e.getMessage());
    }
    return 0;
}

// GET LOW STOCK PRODUCTS LIST
public List<Product> getLowStockProducts() {
    List<Product> list = new ArrayList<>();
    String sql = """
            SELECT id, name, category, unit,
                   selling_price, stock_quantity,
                   low_stock_level, sync_status
            FROM products
            WHERE stock_quantity <= low_stock_level
            ORDER BY stock_quantity ASC
            """;
    try (Connection connection = DatabaseManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
            list.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("unit"),
                    rs.getDouble("selling_price"),
                    rs.getDouble("stock_quantity"),
                    rs.getDouble("low_stock_level"),
                    rs.getString("sync_status")
            ));
        }
    } catch (SQLException e) {
        System.err.println("Error fetching low stock products: " + e.getMessage());
    }
    return list;
}


}

