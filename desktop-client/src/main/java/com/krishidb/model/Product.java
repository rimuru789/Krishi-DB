package com.krishidb.model;

public class Product {

    // Private fields = Encapsulation
    private int id;
    private String name;
    private String category;
    private String unit;
    private double sellingPrice;
    private double stockQuantity;
    private double lowStockLevel;
    private String syncStatus;

    // Constructor used when creating a NEW product
    public Product(String name, String category, String unit,
                   double sellingPrice, double stockQuantity,
                   double lowStockLevel) {

        this.name = name;
        this.category = category;
        this.unit = unit;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
        this.lowStockLevel = lowStockLevel;
        this.syncStatus = "PENDING";
    }

    // Constructor used when reading an EXISTING product from database
    public Product(int id, String name, String category, String unit,
                   double sellingPrice, double stockQuantity,
                   double lowStockLevel, String syncStatus) {

        this.id = id;
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
        this.lowStockLevel = lowStockLevel;
        this.syncStatus = syncStatus;
    }

    public int getId() {
        return id;
    }

    

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getUnit() {
        return unit;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public double getStockQuantity() {
        return stockQuantity;
    }

    public double getLowStockLevel() {
        return lowStockLevel;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setStockQuantity(double stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setLowStockLevel(double lowStockLevel) {
        this.lowStockLevel = lowStockLevel;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public void setId(int id){
    this.id = id;
}

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", unit='" + unit + '\'' +
                ", sellingPrice=" + sellingPrice +
                ", stockQuantity=" + stockQuantity +
                ", lowStockLevel=" + lowStockLevel +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}