package com.krishidb.dao;


import com.krishidb.database.DatabaseManager;
import com.krishidb.model.Product;
import java.net.http.*;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class SyncDAO {


    public List<Product> getPendingProducts(){

        List<Product> products = new ArrayList<>();


        String sql =
                """
                SELECT id,
                name,
                category,
                unit,
                selling_price,
                stock_quantity,
                low_stock_level,
                sync_status
                FROM products
                WHERE sync_status='PENDING'
                """;


        try(
            Connection connection =
                    DatabaseManager.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql);

            ResultSet rs =
                    statement.executeQuery()
        ){


            while(rs.next()){


                Product product =
                        new Product(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("category"),
                                rs.getString("unit"),
                                rs.getDouble("selling_price"),
                                rs.getDouble("stock_quantity"),
                                rs.getDouble("low_stock_level"),
                                rs.getString("sync_status")
                        );


                products.add(product);

            }


        }
        catch(SQLException e){

            e.printStackTrace();

        }


        return products;

    }

    public boolean syncProductsToServer(){

    try{

        List<Product> products = getPendingProducts();

        if(products.isEmpty()){
            return true;
        }


        ObjectMapper mapper = new ObjectMapper();


        HttpClient client =
                HttpClient.newHttpClient();



        
            for(Product product : products){

    System.out.println(
        "Sending product ID: "
        + product.getId()
        + " Name: "
        + product.getName()
    );



    

Product syncProduct = new Product(
        product.getName(),
        product.getCategory(),
        product.getUnit(),
        product.getSellingPrice(),
        product.getStockQuantity(),
        product.getLowStockLevel()
);


String json =
        mapper.writeValueAsString(syncProduct);



            HttpRequest request =
                    HttpRequest.newBuilder()
                    .uri(
                        URI.create(
                        "http://localhost:8080/api/products"
                        )
                    )
                    .header(
                        "Content-Type",
                        "application/json"
                    )
                    .POST(
                        HttpRequest.BodyPublishers.ofString(json)
                    )
                    .build();



            HttpResponse<String> response =
                    client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                    );



            if(response.statusCode()==200){

    boolean updated = markAsSynced(product.getId());

    System.out.println(
            product.getName()
            + " sync update = "
            + updated
    );

}
else{

    System.out.println(
            "Failed syncing product: "
            + product.getName()
            + " Status: "
            + response.statusCode()
    );

}

        }


        return true;


    }
    catch(Exception e){

        e.printStackTrace();
        return false;

    }

}



    public boolean markAsSynced(int id){

    System.out.println("INSIDE markAsSynced ID = " + id);

    String sql =
            """
            UPDATE products
            SET sync_status='SYNCED'
            WHERE id=?
            """;


    try(
        Connection connection =
                DatabaseManager.getConnection();

        PreparedStatement statement =
                connection.prepareStatement(sql)

    ){

        statement.setInt(1,id);

        int rows = statement.executeUpdate();

        System.out.println("Database rows updated = " + rows);

        return rows > 0;


    }
    catch(SQLException e){

        e.printStackTrace();

    }


    return false;

}

}