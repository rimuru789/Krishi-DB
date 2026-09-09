package com.krishidb.model;

public class PurchaseItem {

    private int id;
    private int purchaseId;
    private int productId;
    private String productName;
    private String unit;
    private double quantity;
    private double pricePerUnit;
    private double subtotal;

    public PurchaseItem() {
    }

    // Constructor for creating item before purchase is saved
    public PurchaseItem(int productId, String productName, String unit,
                        double quantity, double pricePerUnit) {
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.subtotal = Math.round(quantity * pricePerUnit * 100.0) / 100.0;
    }

    // Full constructor for database records
    public PurchaseItem(int id, int purchaseId, int productId, String productName,
                        String unit, double quantity, double pricePerUnit, double subtotal) {
        this.id = id;
        this.purchaseId = purchaseId;
        this.productId = productId;
        this.productName = productName;
        this.unit = unit;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.subtotal = subtotal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.subtotal = Math.round(quantity * this.pricePerUnit * 100.0) / 100.0;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
        this.subtotal = Math.round(this.quantity * pricePerUnit * 100.0) / 100.0;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "PurchaseItem{" +
                "id=" + id +
                ", purchaseId=" + purchaseId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                " " + unit +
                ", pricePerUnit=" + pricePerUnit +
                ", subtotal=" + subtotal +
                '}';
    }
}
