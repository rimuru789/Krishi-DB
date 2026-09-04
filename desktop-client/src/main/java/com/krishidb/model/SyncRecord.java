package com.krishidb.model;


public class SyncRecord {

    private String tableName;
    private String operation;
    private String status;
    private String createdAt;


    public SyncRecord(
            String tableName,
            String operation,
            String status,
            String createdAt
    ){

        this.tableName = tableName;
        this.operation = operation;
        this.status = status;
        this.createdAt = createdAt;

    }


    public String getTableName(){
        return tableName;
    }


    public String getOperation(){
        return operation;
    }


    public String getStatus(){
        return status;
    }


    public String getCreatedAt(){
        return createdAt;
    }

}