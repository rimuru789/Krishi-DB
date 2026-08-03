package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;

import java.sql.*;

public class SyncQueueDAO {


    public void addToQueue(
            String tableName,
            int recordId,
            String operation
    ){

        String sql =
        """
        INSERT INTO sync_queue
        (table_name, record_id, operation, status, attempts)
        VALUES (?, ?, ?, 'PENDING', 0)
        """;


        try(Connection connection =
                DatabaseManager.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(sql)){


            statement.setString(1, tableName);
            statement.setInt(2, recordId);
            statement.setString(3, operation);


            statement.executeUpdate();


        }
        catch(SQLException e){

            e.printStackTrace();

        }

    }

}