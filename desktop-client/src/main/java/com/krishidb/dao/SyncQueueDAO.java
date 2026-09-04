package com.krishidb.dao;

import com.krishidb.database.DatabaseManager;
import com.krishidb.model.SyncRecord;
import java.util.ArrayList;
import java.util.List;

import java.sql.*;

public class SyncQueueDAO {

    public List<SyncRecord> getSyncHistory(){

    List<SyncRecord> history = new ArrayList<>();


    String sql =
    """
    SELECT
    table_name,
    operation,
    status,
    created_at
    FROM sync_queue
    ORDER BY created_at DESC
    LIMIT 10
    """;


    try(Connection connection =
            DatabaseManager.getConnection();

        PreparedStatement statement =
            connection.prepareStatement(sql);

        ResultSet rs =
            statement.executeQuery()
    ){


        while(rs.next()){


            SyncRecord record =
                    new SyncRecord(
                            rs.getString("table_name"),
                            rs.getString("operation"),
                            rs.getString("status"),
                            rs.getString("created_at")
                    );


            history.add(record);

        }


    }
    catch(SQLException e){

        e.printStackTrace();

    }


    return history;

}
    
    
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