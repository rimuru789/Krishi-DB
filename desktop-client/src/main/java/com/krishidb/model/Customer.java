package com.krishidb.model;

public class Customer {

    private int id;
    private String name;
    private String phone;
    private String village;
    private String createdAt;
    private String updatedAt;
    private String syncStatus;

    // Constructor for new customer creation
    public Customer(String name, String phone, String village) {
        this.name = name;
        this.phone = phone;
        this.village = village;
        this.syncStatus = "PENDING";
    }

    // Constructor for reading existing customer from SQLite
    public Customer(int id, String name, String phone, String village,
                    String createdAt, String updatedAt, String syncStatus) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.village = village;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.syncStatus = syncStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", village='" + village + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
