package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getCategory());
            statement.setString(3, product.getUnit());
            statement.setDouble(4, product.getSellingPrice());
            statement.setDouble(5, product.getStockQuantity());
            statement.setDouble(6, product.getLowStockLevel());
            statement.setString(7, product.getSyncStatus());

            int rowsAffected = statement.executeUpdate();

            return rowsAffected > 0;

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
                low_stock_level=?
            WHERE id=?
            """;


    try(Connection connection = DatabaseManager.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {


        statement.setString(1, product.getName());
        statement.setString(2, product.getCategory());
        statement.setString(3, product.getUnit());
        statement.setDouble(4, product.getSellingPrice());
        statement.setDouble(5, product.getStockQuantity());
        statement.setDouble(6, product.getLowStockLevel());
        statement.setInt(7, product.getId());


        return statement.executeUpdate() > 0;


    } catch(SQLException e){

        e.printStackTrace();
        return false;
    }
}




// DELETE PRODUCT

public boolean deleteProduct(int id) {

    String sql =
            "DELETE FROM products WHERE id = ?";


    try(Connection connection =
                DatabaseManager.getConnection();

        PreparedStatement statement =
                connection.prepareStatement(sql)) {


        statement.setInt(1, id);


        int rows =
                statement.executeUpdate();


        return rows > 0;


    } catch(SQLException e){

        System.out.println(
                "Failed to delete product."
        );

        e.printStackTrace();

        return false;
    }
}


}

