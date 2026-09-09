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

    public static class SyncResult {
        private final int totalAttempted;
        private final int successCount;
        private final int failureCount;
        private final boolean serverUnavailable;
        private final String message;

        public SyncResult(int totalAttempted, int successCount, int failureCount, boolean serverUnavailable, String message) {
            this.totalAttempted = totalAttempted;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.serverUnavailable = serverUnavailable;
            this.message = message;
        }

        public int getTotalAttempted() { return totalAttempted; }
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public boolean isServerUnavailable() { return serverUnavailable; }
        public String getMessage() { return message; }

        public boolean isAllSuccess() {
            return !serverUnavailable && totalAttempted > 0 && failureCount == 0;
        }

        public boolean isPartialSuccess() {
            return !serverUnavailable && successCount > 0 && failureCount > 0;
        }
    }

    public boolean syncProductsToServer() {
        SyncResult res = syncProductsWithResult();
        return res.isAllSuccess() || (res.getTotalAttempted() == 0 && !res.isServerUnavailable());
    }

    public SyncResult syncProductsWithResult() {
        List<Product> products = getPendingProducts();

        if (products.isEmpty()) {
            return new SyncResult(0, 0, 0, false, "No pending products to sync");
        }

        SettingsDAO settingsDAO = new SettingsDAO();
        String baseUrl = settingsDAO.getSetting("server_url", "http://localhost:8080").trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String targetUrl = baseUrl + "/api/products";

        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();

        int successCount = 0;
        int failureCount = 0;
        boolean serverUnavailable = false;

        for (Product product : products) {
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

            try {
                String json = mapper.writeValueAsString(syncProduct);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(targetUrl))
                        .header("Content-Type", "application/json")
                        .timeout(java.time.Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                int statusCode = response.statusCode();

                // Treat complete 2xx range (200 through 299) as successful
                if (statusCode >= 200 && statusCode < 300) {
                    boolean updated = markAsSynced(product.getId());
                    boolean queueUpdated = markQueueCompleted(product.getId());
                    successCount++;

                    System.out.println(
                        "Successfully synced product: "
                        + product.getName()
                        + " Status: "
                        + statusCode
                    );
                } else {
                    failureCount++;
                    recordQueueFailure(product.getId());

                    System.out.println(
                        "Failed syncing product: "
                        + product.getName()
                        + " Status: "
                        + statusCode
                    );
                }
            } catch (java.net.ConnectException | java.net.http.HttpConnectTimeoutException e) {
                serverUnavailable = true;
                failureCount++;
                recordQueueFailure(product.getId());
                System.err.println(
                    "Failed syncing product: "
                    + product.getName()
                    + " Server unavailable: "
                    + e.getMessage()
                );
                break; // Server is unreachable, stop attempting remaining items in this batch
            } catch (Exception e) {
                failureCount++;
                recordQueueFailure(product.getId());
                System.err.println(
                    "Failed syncing product: "
                    + product.getName()
                    + " Error: "
                    + e.getMessage()
                );
            }
        }

        String summary = "Sync finished: " + successCount + " succeeded, " + failureCount + " failed.";
        return new SyncResult(products.size(), successCount, failureCount, serverUnavailable, summary);
    }

public ResultSet getPendingQueue(){

    String sql =
    """
    SELECT *
    FROM sync_queue
    WHERE status='PENDING'
    """;


    try {

        Connection connection =
                DatabaseManager.getConnection();

        PreparedStatement statement =
                connection.prepareStatement(sql);


        return statement.executeQuery();


    }
    catch(SQLException e){

        e.printStackTrace();

    }


    return null;
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

public List<Integer> getPendingQueueIds(){

    List<Integer> ids = new ArrayList<>();


    String sql =
            """
            SELECT record_id
            FROM sync_queue
            WHERE status='PENDING'
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

            ids.add(
                rs.getInt("record_id")
            );

        }


    }
    catch(SQLException e){

        e.printStackTrace();

    }


    return ids;

}

    public boolean markQueueCompleted(int productId) {
        String sql = """
                UPDATE sync_queue
                SET status = 'COMPLETED', last_attempt_at = CURRENT_TIMESTAMP
                WHERE table_name = 'products' AND record_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean recordQueueFailure(int productId) {
        String sql = """
                UPDATE sync_queue
                SET attempts = attempts + 1, last_attempt_at = CURRENT_TIMESTAMP
                WHERE table_name = 'products' AND record_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}